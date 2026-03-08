package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.subsystems.FlywheelLauncher;
import org.firstinspires.ftc.teamcode.util.BallisticsCalculator;

import java.util.List;

/**
 * Vision-guided launcher OpMode
 * Uses Limelight 3A to detect AprilTags and calculate optimal flywheel speed
 *
 * Controls:
 * - Right Trigger: Spin up flywheel (vision-based speed)
 * - A Button: Manual launch (test mode)
 * - B Button: Stop flywheel
 * - DPad Up/Down: Manual RPM adjustment (testing)
 */
@TeleOp(name = "Vision Launcher", group = "Competition")
public class VisionLauncherOpMode extends LinearOpMode {

    // Hardware
    private Limelight3A limelight;
    private FlywheelLauncher launcher;

    // Configuration (ADJUST THESE TO YOUR SETUP)
    private static final String LIMELIGHT_NAME = "limelight";
    private static final String FLYWHEEL_MOTOR_NAME = "flywheel";

    // AprilTag configuration
    private static final int TARGET_APRILTAG_ID = 23; // Change to your target tag ID
    private static final double APRILTAG_HEIGHT = 0.57; // Height of AprilTag from ground (meters)
    private static final double LIMELIGHT_HEIGHT = 0.46; // Height of Limelight from ground (meters)
    private static final double LIMELIGHT_ANGLE = 30.0; // Limelight mount angle (degrees, positive = tilted up)

    // Operational parameters
    private static final double RPM_TOLERANCE = 50.0; // RPM tolerance for "at speed"
    private static final double MIN_SPIN_TIME = 0.5; // Minimum time at speed before shooting (seconds)
    private static final double RPM_INCREMENT = 100.0; // Manual RPM adjustment amount

    // State variables
    private double manualRPM = 2000.0; // Default RPM for manual mode
    private boolean visionMode = true; // true = vision-based, false = manual RPM

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize hardware with error handling
        telemetry.addData("Status", "Initializing hardware...");
        telemetry.update();

        try {
            limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);
            telemetry.addData("Limelight", "✓ Connected");
        } catch (Exception e) {
            telemetry.addData("ERROR", "Limelight not found!");
            telemetry.addData("Check", "1. Limelight is plugged in");
            telemetry.addData("Check", "2. Config name is '%s'", LIMELIGHT_NAME);
            telemetry.update();
            throw e;
        }

        try {
            launcher = new FlywheelLauncher(hardwareMap, FLYWHEEL_MOTOR_NAME);
            telemetry.addData("Flywheel", "✓ Connected");
        } catch (Exception e) {
            telemetry.addData("ERROR", "Flywheel motor not found!");
            telemetry.addData("Check", "1. Motor is connected");
            telemetry.addData("Check", "2. Config name is '%s'", FLYWHEEL_MOTOR_NAME);
            telemetry.addData("Exception", e.getMessage());
            telemetry.update();
            throw e;
        }

        // Configure Limelight
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0); // Use pipeline 0 (configure for AprilTag detection)
        limelight.start();
        telemetry.addData("Status", "Ready!");
        telemetry.addData(">", "Press Play to start");
        telemetry.update();

        waitForStart();

        // Main loop
        while (opModeIsActive()) {

            // Get Limelight status and results
            LLStatus status = limelight.getStatus();
            LLResult result = limelight.getLatestResult();

            // Process controls
            handleDriverControls();

            // Process vision data if available
            if (result != null && result.isValid()) {
                processVisionData(result);
            }

            // Update telemetry
            updateTelemetry(status, result);
        }

        // Cleanup
        launcher.stop();
        limelight.stop();
    }

    /**
     * Handle gamepad controls
     */
    private void handleDriverControls() {
        Gamepad gamepad = gamepad1;

        // Right Trigger: Spin up flywheel
        if (gamepad.right_trigger > 0.5) {
            if (visionMode) {
                telemetry.addData("Mode", "Vision-based launch (hold trigger)");
            } else {
                launcher.setTargetRPM(manualRPM);
                telemetry.addData("Mode", "Manual launch at %.0f RPM", manualRPM);
            }
        }
        // B Button: Stop flywheel
        else if (gamepad.b) {
            launcher.stop();
            telemetry.addData("Mode", "Stopped");
        }

        // Toggle vision/manual mode with X
        if (gamepad.x) {
            visionMode = !visionMode;
            sleep(200); // Debounce
            telemetry.addData("Mode Changed", visionMode ? "VISION" : "MANUAL");
        }

        // Manual RPM adjustment (DPad Up/Down)
        if (gamepad.dpad_up) {
            manualRPM += RPM_INCREMENT;
            sleep(100);
        } else if (gamepad.dpad_down) {
            manualRPM -= RPM_INCREMENT;
            manualRPM = Math.max(0, manualRPM); // Don't go negative
            sleep(100);
        }

        // Emergency stop (both bumpers)
        if (gamepad.left_bumper && gamepad.right_bumper) {
            launcher.stop();
            telemetry.addData("EMERGENCY", "STOP ACTIVATED");
        }
    }

    /**
     * Process Limelight vision data to control launcher
     */
    private void processVisionData(LLResult result) {
        // Get AprilTag detections
        List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();

        if (fiducialResults.isEmpty()) {
            telemetry.addData("Vision", "No AprilTags detected");
            return;
        }

        // Find our target AprilTag
        LLResultTypes.FiducialResult targetTag = null;
        for (LLResultTypes.FiducialResult tag : fiducialResults) {
            if (tag.getFiducialId() == TARGET_APRILTAG_ID) {
                targetTag = tag;
                break;
            }
        }

        if (targetTag == null) {
            telemetry.addData("Vision", "Target tag #%d not found", TARGET_APRILTAG_ID);
            return;
        }

        // Calculate distance to target
        double tx = targetTag.getTargetXDegrees();
        double ty = targetTag.getTargetYDegrees();

        double distance = BallisticsCalculator.calculateDistanceFromAprilTag(
                tx, ty,
                LIMELIGHT_HEIGHT,
                APRILTAG_HEIGHT,
                LIMELIGHT_ANGLE);

        telemetry.addData("Vision", "Tag #%d detected", TARGET_APRILTAG_ID);
        telemetry.addData("Distance", "%.2f meters", distance);
        telemetry.addData("Offset", "X: %.2f°, Y: %.2f°", tx, ty);

        // Set launcher speed based on distance (if trigger held)
        if (visionMode && gamepad1.right_trigger > 0.5) {
            launcher.setSpeedForDistance(distance);

            // Check if ready to shoot
            if (launcher.isReadyToShoot(RPM_TOLERANCE, MIN_SPIN_TIME)) {
                telemetry.addData("Status", ">>> READY TO SHOOT! <<<");
                // Optional: Add LED indicator, rumble, or sound
                gamepad1.rumble(100);
            } else {
                telemetry.addData("Status", "Spinning up...");
            }
        }
    }

    /**
     * Update telemetry with comprehensive status info
     */
    private void updateTelemetry(LLStatus status, LLResult result) {
        // Limelight status
        if (status != null) {
            telemetry.addData("Limelight", "%s | %.1f°C | %.0f FPS",
                    status.getName(),
                    status.getTemp(),
                    status.getFps());
        }

        // Launcher status
        telemetry.addData("---", "LAUNCHER STATUS");
        telemetry.addData("Current RPM", "%.0f", launcher.getCurrentRPM());
        telemetry.addData("Target RPM", "%.0f", launcher.getTargetRPM());
        telemetry.addData("Error", "%.0f RPM", launcher.getError());
        telemetry.addData("At Speed?", launcher.isAtSpeed(RPM_TOLERANCE) ? "YES" : "NO");

        // Controls info
        telemetry.addData("---", "CONTROLS");
        telemetry.addData("Mode", visionMode ? "VISION" : "MANUAL (%.0f RPM)", manualRPM);
        telemetry.addData("RT", "Spin up | B: Stop | X: Toggle mode");
        telemetry.addData("DPad", "Up/Down: Adjust manual RPM");

        telemetry.update();
    }
}