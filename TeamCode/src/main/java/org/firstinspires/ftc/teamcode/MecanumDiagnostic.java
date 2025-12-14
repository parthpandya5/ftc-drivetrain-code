package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Mecanum Diagnostic - Test individual motor directions
 *
 * This will help identify if motors are wired correctly
 * and if mecanum wheels are installed properly.
 *
 * D-PAD CONTROLS:
 * - D-Pad UP: All motors forward (robot should go straight forward)
 * - D-Pad DOWN: All motors backward (robot should go straight backward)
 * - D-Pad LEFT: Test strafe left pattern
 * - D-Pad RIGHT: Test strafe right pattern
 *
 * BUTTON CONTROLS (individual motor test) - Rev USB Gamepad:
 * - X (Bottom): Front Left only
 * - B (Right): Front Right only
 * - Y (Top): Rear Left only
 * - A (Left): Rear Right only
 */
@TeleOp(name = "Mecanum Diagnostic", group = "Test")
public class MecanumDiagnostic extends LinearOpMode {

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private final double TEST_POWER = 0.5;

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize motors
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        rearRight = hardwareMap.get(DcMotor.class, "rearRight");

        // Set all to FORWARD for now (we'll test different configs)
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.FORWARD);
        rearRight.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized - Diagnostic Mode");
        telemetry.addData("D-Pad", "Test movement patterns");
        telemetry.addData("Buttons", "Test individual motors");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // Reset all motors
            double fl = 0, fr = 0, rl = 0, rr = 0;
            String mode = "Idle";

            // D-PAD TESTS - Movement patterns
            if (gamepad1.dpad_up) {
                // Forward - all motors same direction
                fl = TEST_POWER;
                fr = TEST_POWER;
                rl = TEST_POWER;
                rr = TEST_POWER;
                mode = "Forward Test";
            }
            else if (gamepad1.dpad_down) {
                // Backward - all motors opposite direction
                fl = -TEST_POWER;
                fr = -TEST_POWER;
                rl = -TEST_POWER;
                rr = -TEST_POWER;
                mode = "Backward Test";
            }
            else if (gamepad1.dpad_right) {
                // Strafe RIGHT pattern for mecanum
                // FL and RR should go FORWARD
                // FR and RL should go BACKWARD
                fl = TEST_POWER;   // Forward
                fr = -TEST_POWER;  // Backward
                rl = -TEST_POWER;  // Backward
                rr = TEST_POWER;   // Forward
                mode = "Strafe RIGHT Test (FL+, FR-, RL-, RR+)";
            }
            else if (gamepad1.dpad_left) {
                // Strafe LEFT pattern for mecanum
                // FL and RR should go BACKWARD
                // FR and RL should go FORWARD
                fl = -TEST_POWER;  // Backward
                fr = TEST_POWER;   // Forward
                rl = TEST_POWER;   // Forward
                rr = -TEST_POWER;  // Backward
                mode = "Strafe LEFT Test (FL-, FR+, RL+, RR-)";
            }
            // BUTTON TESTS - Individual motors (Rev USB Gamepad layout)
            else if (gamepad1.x) {  // Bottom button
                fl = TEST_POWER;
                mode = "Testing: Front Left ONLY (X button)";
            }
            else if (gamepad1.b) {  // Right button
                fr = TEST_POWER;
                mode = "Testing: Front Right ONLY (B button)";
            }
            else if (gamepad1.y) {  // Top button
                rl = TEST_POWER;
                mode = "Testing: Rear Left ONLY (Y button)";
            }
            else if (gamepad1.a) {  // Left button
                rr = TEST_POWER;
                mode = "Testing: Rear Right ONLY (A button)";
            }

            // Set motor powers
            frontLeft.setPower(fl);
            frontRight.setPower(fr);
            rearLeft.setPower(rl);
            rearRight.setPower(rr);

            // Display telemetry
            telemetry.addData("Mode", mode);
            telemetry.addData("---", "---");
            telemetry.addData("FL Power", "%.2f", fl);
            telemetry.addData("FR Power", "%.2f", fr);
            telemetry.addData("RL Power", "%.2f", rl);
            telemetry.addData("RR Power", "%.2f", rr);
            telemetry.addData("---", "---");
            telemetry.addData("EXPECTED BEHAVIOR", "");
            telemetry.addData("D-Pad UP", "Robot goes straight FORWARD");
            telemetry.addData("D-Pad DOWN", "Robot goes straight BACKWARD");
            telemetry.addData("D-Pad RIGHT", "Robot slides RIGHT (no rotation)");
            telemetry.addData("D-Pad LEFT", "Robot slides LEFT (no rotation)");
            telemetry.update();
        }

        // Stop all motors
        frontLeft.setPower(0);
        frontRight.setPower(0);
        rearLeft.setPower(0);
        rearRight.setPower(0);
    }
}