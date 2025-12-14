package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Motor Direction Finder
 *
 * This will help us find the EXACT motor direction configuration you need.
 *
 * CONTROLS (Rev USB Gamepad):
 * - D-Pad UP: Test all motors FORWARD at 0.5 power
 * - D-Pad DOWN: Test all motors BACKWARD at -0.5 power
 *
 * BUMPERS to change configurations:
 * - Left Bumper: Previous configuration
 * - Right Bumper: Next configuration
 *
 * Watch the robot and find which configuration makes ALL wheels spin forward together.
 */
@TeleOp(name = "Motor Direction Finder", group = "Test")
public class Motordirectionfinder extends LinearOpMode {

    private DcMotor frontLeft, frontRight, rearLeft, rearRight;
    private int configIndex = 0;

    // All possible motor direction configurations
    private final String[][] configs = {
            // Config 0 - All FORWARD
            {"FORWARD", "FORWARD", "FORWARD", "FORWARD"},
            // Config 1 - Standard mecanum (left reversed)
            {"REVERSE", "FORWARD", "REVERSE", "FORWARD"},
            // Config 2 - Only rearLeft forward
            {"REVERSE", "FORWARD", "REVERSE", "REVERSE"},
            // Config 3 - rearLeft forward, frontLeft forward
            {"FORWARD", "FORWARD", "REVERSE", "FORWARD"},
            // Config 4 - All reversed
            {"REVERSE", "REVERSE", "REVERSE", "REVERSE"},
            // Config 5 - Right side reversed
            {"FORWARD", "REVERSE", "FORWARD", "REVERSE"},
            // Config 6
            {"FORWARD", "REVERSE", "FORWARD", "FORWARD"},
            // Config 7
            {"REVERSE", "REVERSE", "FORWARD", "FORWARD"}
    };

    private boolean lastLeftBumper = false;
    private boolean lastRightBumper = false;

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize motors
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        rearRight = hardwareMap.get(DcMotor.class, "rearRight");

        telemetry.addData("Status", "Ready to find motor directions");
        telemetry.addData("", "Use bumpers to change config");
        telemetry.addData("", "D-Pad UP to test");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // Change configuration with bumpers
            if (gamepad1.left_bumper && !lastLeftBumper) {
                configIndex--;
                if (configIndex < 0) configIndex = configs.length - 1;
                applyConfiguration();
            }
            if (gamepad1.right_bumper && !lastRightBumper) {
                configIndex++;
                if (configIndex >= configs.length) configIndex = 0;
                applyConfiguration();
            }
            lastLeftBumper = gamepad1.left_bumper;
            lastRightBumper = gamepad1.right_bumper;

            // Test motors with D-Pad
            if (gamepad1.dpad_up) {
                frontLeft.setPower(0.5);
                frontRight.setPower(0.5);
                rearLeft.setPower(0.5);
                rearRight.setPower(0.5);
            }
            else if (gamepad1.dpad_down) {
                frontLeft.setPower(-0.5);
                frontRight.setPower(-0.5);
                rearLeft.setPower(-0.5);
                rearRight.setPower(-0.5);
            }
            else {
                frontLeft.setPower(0);
                frontRight.setPower(0);
                rearLeft.setPower(0);
                rearRight.setPower(0);
            }

            // Display current configuration
            telemetry.addData("Configuration", "%d of %d", configIndex + 1, configs.length);
            telemetry.addData("", "");
            telemetry.addData("Front Left", configs[configIndex][0]);
            telemetry.addData("Front Right", configs[configIndex][1]);
            telemetry.addData("Rear Left", configs[configIndex][2]);
            telemetry.addData("Rear Right", configs[configIndex][3]);
            telemetry.addData("", "");
            telemetry.addData("Controls", "Left/Right Bumper: Change config");
            telemetry.addData("", "D-Pad UP: Test forward");
            telemetry.addData("", "D-Pad DOWN: Test backward");
            telemetry.addData("", "");
            telemetry.addData("Goal", "Find config where D-Pad UP");
            telemetry.addData("", "makes ALL wheels go forward");
            telemetry.update();
        }

        // Stop all motors
        frontLeft.setPower(0);
        frontRight.setPower(0);
        rearLeft.setPower(0);
        rearRight.setPower(0);
    }

    private void applyConfiguration() {
        frontLeft.setDirection(
                configs[configIndex][0].equals("FORWARD") ?
                        DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE
        );
        frontRight.setDirection(
                configs[configIndex][1].equals("FORWARD") ?
                        DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE
        );
        rearLeft.setDirection(
                configs[configIndex][2].equals("FORWARD") ?
                        DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE
        );
        rearRight.setDirection(
                configs[configIndex][3].equals("FORWARD") ?
                        DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE
        );
    }
}