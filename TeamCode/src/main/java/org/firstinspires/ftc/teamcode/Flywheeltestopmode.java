package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.FlywheelLauncher;

/**
 * Simple test OpMode for flywheel motor
 * Use this to verify motor works and tune PID before adding vision
 *
 * Controls:
 * - DPad Up: Increase target RPM by 100
 * - DPad Down: Decrease target RPM by 100
 * - A Button: Spin to target RPM
 * - B Button: Stop motor
 * - Left Stick Y: Manual power control (for testing)
 */
@TeleOp(name = "Test: Flywheel Only", group = "Testing")
public class FlywheelTestOpMode extends LinearOpMode {

    private FlywheelLauncher launcher;
    private double targetRPM = 1000.0;
    private boolean useVelocityMode = true;

    @Override
    public void runOpMode() throws InterruptedException {

        telemetry.addData("Status", "Initializing...");
        telemetry.update();

        // Initialize launcher (change "flywheel" to match your config)
        try {
            launcher = new FlywheelLauncher(hardwareMap, "flywheel");
            telemetry.addData("Flywheel", "✓ Connected");
        } catch (Exception e) {
            telemetry.addData("ERROR", "Motor 'flywheel' not found!");
            telemetry.addData("Fix", "Configure motor in Driver Station");
            telemetry.addData("Exception", e.getMessage());
            telemetry.update();
            throw e;
        }

        telemetry.addData("Status", "Ready!");
        telemetry.addData(">", "Press Play to start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // RPM adjustment
            if (gamepad1.dpad_up) {
                targetRPM += 100;
                sleep(100); // Debounce
            } else if (gamepad1.dpad_down) {
                targetRPM -= 100;
                targetRPM = Math.max(0, targetRPM);
                sleep(100);
            }

            // Large RPM jumps
            if (gamepad1.y) {
                targetRPM += 500;
                sleep(200);
            } else if (gamepad1.a && !gamepad1.b) {
                targetRPM -= 500;
                targetRPM = Math.max(0, targetRPM);
                sleep(200);
            }

            // Spin up to target RPM
            if (gamepad1.right_trigger > 0.5) {
                launcher.setTargetRPM(targetRPM);
                useVelocityMode = true;
            }

            // Stop
            if (gamepad1.b) {
                launcher.stop();
                useVelocityMode = true;
            }

            // Manual power mode (for basic testing)
            if (Math.abs(gamepad1.left_stick_y) > 0.1) {
                double power = -gamepad1.left_stick_y; // Invert Y axis
                launcher.setPower(power);
                useVelocityMode = false;
            }

            // Preset speeds for testing
            if (gamepad1.x) {
                targetRPM = 1500;
                launcher.setTargetRPM(targetRPM);
                sleep(200);
            }

            // Display telemetry
            telemetry.addData("---", "FLYWHEEL STATUS");
            telemetry.addData("Mode", useVelocityMode ? "VELOCITY (PID)" : "MANUAL POWER");
            telemetry.addData("Current RPM", "%.0f", launcher.getCurrentRPM());
            telemetry.addData("Target RPM", "%.0f", targetRPM);
            telemetry.addData("Error", "%.0f RPM", launcher.getError());
            telemetry.addData("Motor Power", "%.3f", launcher.getMotorPower());
            telemetry.addData("At Speed?", launcher.isAtSpeed(50) ? "YES ✓" : "NO");

            telemetry.addData("---", "CONTROLS");
            telemetry.addData("DPad ↑↓", "Adjust RPM (±100)");
            telemetry.addData("Y / A", "Large adjust (±500)");
            telemetry.addData("RT", "Spin to target | B: Stop");
            telemetry.addData("Left Stick Y", "Manual power");
            telemetry.addData("X", "Preset: 1500 RPM");

            telemetry.addData("---", "TESTING TIPS");
            telemetry.addData("1", "Use DPad to set desired RPM");
            telemetry.addData("2", "Hold RT to spin up");
            telemetry.addData("3", "Watch 'At Speed?' indicator");
            telemetry.addData("4", "Tune PID if unstable");

            telemetry.update();
        }

        launcher.stop();
    }
}