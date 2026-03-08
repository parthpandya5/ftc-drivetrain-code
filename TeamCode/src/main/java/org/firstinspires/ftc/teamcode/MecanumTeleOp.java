package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.FlywheelLauncher;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * MecanumTeleOp - Main TeleOp OpMode
 *
 * Driver 1 Controls:
 * - Left Stick Y: Forward/Backward
 * - Left Stick X: Strafe Left/Right
 * - Right Stick X: Rotate
 * - Left Bumper: Precision/Slow Mode (40% speed)
 * - Right Trigger: Intake In
 * - Left Trigger: Intake Out (Unjam)
 * - A Button: Spin up Shooter (Flywheel)
 * - B Button: Stop Shooter
 */
@TeleOp(name = "Mecanum TeleOp", group = "TeleOp")
public class MecanumTeleOp extends LinearOpMode {

    // Declare subsystems
    private MecanumDrivetrain drivetrain;
    private Intake intake;
    private FlywheelLauncher shooter;

    // State
    private double shooterTargetRPM = 2000.0; // Default RPM for manual launch

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize Drivetrain
        drivetrain = new MecanumDrivetrain(hardwareMap);
        drivetrain.setTelemetry(telemetry);

        // Initialize Mechanisms
        try {
            intake = new Intake(hardwareMap, "intake");
            shooter = new FlywheelLauncher(hardwareMap, "shooter");
        } catch (IllegalArgumentException e) {
            telemetry.addData("HARDWARE ERROR", "Missing motor config!");
            telemetry.addData("Exception", e.getMessage());
            telemetry.update();
            // Don't crash immediately, allow user to read error
            sleep(5000);
            throw e;
        }

        // Display initialization status
        telemetry.addData("Status", "Initialized Successfully");
        telemetry.addData("---", "DRIVE CONTROLS");
        telemetry.addData("L Stick", "Move & Strafe");
        telemetry.addData("R Stick X", "Rotate");
        telemetry.addData("L Bumper", "Precision Mode");
        telemetry.addData("---", "MECHANISM CONTROLS");
        telemetry.addData("R Trigger", "Intake In");
        telemetry.addData("L Trigger", "Intake Out");
        telemetry.addData("A/B", "Shooter Start/Stop");
        telemetry.update();

        // Wait for the driver to press START
        waitForStart();

        // Run until the driver presses STOP
        while (opModeIsActive()) {

            // --- DRIVETRAIN CONTROLS ---
            drivetrain.setPrecisionMode(gamepad1.left_bumper);

            // Note: MecanumDrivetrain.driveWithGamepad() already handles
            // the inverted Y-axis. We just pass the raw gamepad inputs here.
            drivetrain.driveWithGamepad(
                    gamepad1.left_stick_y,
                    gamepad1.left_stick_x,
                    gamepad1.right_stick_x);

            // --- INTAKE CONTROLS ---
            // Triggers act as variable speed control
            if (gamepad1.right_trigger > 0.05) {
                intake.setPower(gamepad1.right_trigger);
            } else if (gamepad1.left_trigger > 0.05) {
                intake.setPower(-gamepad1.left_trigger);
            } else {
                intake.stop();
            }

            // --- SHOOTER CONTROLS ---
            if (gamepad1.a) {
                shooter.setTargetRPM(shooterTargetRPM);
            } else if (gamepad1.b) {
                shooter.stop();
            }

            // --- TELEMETRY ---
            drivetrain.sendTelemetry();

            telemetry.addData("---", "MECHANISMS");
            telemetry.addData("Intake Power", "%.2f", intake.getPower());
            telemetry.addData("Shooter RPM", "%.0f / %.0f", shooter.getCurrentRPM(), shooter.getTargetRPM());
            telemetry.addData("Shooter Ready?", shooter.isAtSpeed(50) ? "YES" : "NO");

            telemetry.update();
        }

        // Stop all mechanisms when OpMode ends
        drivetrain.stop();
        if (intake != null)
            intake.stop();
        if (shooter != null)
            shooter.stop();
    }
}