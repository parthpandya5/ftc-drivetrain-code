package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * MecanumTeleOp - TeleOp OpMode using the MecanumDrivetrain class
 *
 * Controls:
 * - Left Stick Y: Forward/Backward
 * - Left Stick X: Strafe Left/Right
 * - Right Stick X: Rotate Left/Right
 * - Left Bumper: Precision/Slow Mode (40% speed)
 */
@TeleOp(name = "Mecanum TeleOp", group = "TeleOp")
public class MecanumTeleOp extends LinearOpMode {

    // Declare drivetrain object
    private MecanumDrivetrain drivetrain;

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize the drivetrain
        drivetrain = new MecanumDrivetrain(hardwareMap);
        drivetrain.setTelemetry(telemetry);

        // Display initialization status
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Controls", "Left Stick Y: Forward/Back");
        telemetry.addData("", "Left Stick X: Strafe Left/Right");
        telemetry.addData("", "Right Stick X: Rotate");
        telemetry.addData("", "Left Bumper: Precision Mode");
        telemetry.update();

        // Wait for the driver to press START
        waitForStart();

        // Run until the driver presses STOP
        while (opModeIsActive()) {

            // Enable precision mode when left bumper is pressed
            drivetrain.setPrecisionMode(gamepad1.left_bumper);

            // Drive the robot using gamepad inputs (inverted to fix reversed controls)
            drivetrain.driveWithGamepad(
                    -gamepad1.left_stick_y,   // Forward/Backward (inverted)
                    -gamepad1.left_stick_x,   // Strafe Left/Right (inverted)
                    -gamepad1.right_stick_x   // Turn Left/Right (inverted)
            );

            // Display drivetrain telemetry
            drivetrain.sendTelemetry();

            // Optional: Display encoder positions
            // drivetrain.sendEncoderTelemetry();

            // Update telemetry display
            telemetry.update();
        }

        // Stop the robot when OpMode ends
        drivetrain.stop();
    }
}