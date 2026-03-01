package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * MecanumDrivetrain - A comprehensive drivetrain class for mecanum wheel robots
 *
 * Features:
 * - Robot-centric and field-centric driving modes
 * - Configurable deadzone filtering
 * - Precision/slow mode support
 * - Automatic power normalization
 * - Encoder tracking and utilities
 * - Telemetry integration
 *
 * Motor Configuration (set these names in your Robot Configuration):
 * - "frontLeft" : Front Left Motor
 * - "frontRight" : Front Right Motor
 * - "rearLeft" : Rear Left Motor (Back Left)
 * - "rearRight" : Rear Right Motor (Back Right)
 */
public class MecanumDrivetrain {

    // ==================== HARDWARE =================
    private final DcMotor frontLeft;
    private final DcMotor frontRight;
    private final DcMotor rearLeft;
    private final DcMotor rearRight;

    // ==================== CONSTANTS ====================
    private static final double DEFAULT_DEADZONE = 0.07;
    private static final double PRECISION_SCALE = 0.4;
    private static final double NORMAL_SCALE = 1.0;

    // ==================== STATE VARIABLES ====================
    private double deadzone = DEFAULT_DEADZONE;
    private double speedScale = NORMAL_SCALE;
    private Telemetry telemetry = null;

    /**
     * Constructor - initializes the mecanum drivetrain
     * @param hardwareMap The OpMode's hardware map
     */
    public MecanumDrivetrain(HardwareMap hardwareMap) {
        // Initialize motors
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        rearRight = hardwareMap.get(DcMotor.class, "rearRight");

        // Set motor directions - Configuration 3 (tested and working)
        frontLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        rearRight.setDirection(DcMotorSimple.Direction.FORWARD);

        // Set zero power behavior to brake for better control
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reset encoders and set run mode
        resetEncoders();
        setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // ==================== PRIMARY DRIVE METHODS ====================

    /**
     * Drive the robot using raw inputs (robot-centric)
     * @param drive Forward/backward movement (-1.0 to 1.0, forward is positive)
     * @param strafe Left/right movement (-1.0 to 1.0, right is positive)
     * @param turn Rotation (-1.0 to 1.0, counter-clockwise is positive)
     */
    public void drive(double drive, double strafe, double turn) {
        // Apply deadzone filtering
        drive = applyDeadzone(drive);
        strafe = applyDeadzone(strafe);
        turn = applyDeadzone(turn);

        // Calculate motor powers using mecanum kinematics
        double fl = drive + strafe + turn;
        double fr = drive - strafe - turn;
        double rl = drive - strafe + turn;
        double rr = drive + strafe - turn;

        // Normalize to ensure no power exceeds 1.0
        double max = Math.max(Math.abs(fl), Math.abs(fr));
        max = Math.max(max, Math.abs(rl));
        max = Math.max(max, Math.abs(rr));

        if (max > 1.0) {
            fl /= max;
            fr /= max;
            rl /= max;
            rr /= max;
        }

        // Apply speed scaling
        fl *= speedScale;
        fr *= speedScale;
        rl *= speedScale;
        rr *= speedScale;

        // Set motor powers
        setPowers(fl, fr, rl, rr);
    }

    /**
     * Drive using gamepad inputs directly (handles gamepad Y-axis inversion)
     * @param leftStickY Gamepad left stick Y axis (forward/backward)
     * @param leftStickX Gamepad left stick X axis (strafe left/right)
     * @param rightStickX Gamepad right stick X axis (turn left/right)
     */
    public void driveWithGamepad(double leftStickY, double leftStickX, double rightStickX) {
        // Gamepad Y-axis is inverted, so negate it
        drive(-leftStickY, leftStickX, rightStickX);
    }

    /**
     * Field-centric drive - movement relative to the field instead of robot
     * Requires IMU for heading measurement
     * @param drive Forward/backward in field frame
     * @param strafe Left/right in field frame
     * @param turn Rotation
     * @param robotHeading Current robot heading in radians (from IMU)
     */
    public void driveFieldCentric(double drive, double strafe, double turn, double robotHeading) {
        // Apply deadzone
        drive = applyDeadzone(drive);
        strafe = applyDeadzone(strafe);
        turn = applyDeadzone(turn);

        // Rotate the input vector by the robot's heading
        double rotX = strafe * Math.cos(-robotHeading) - drive * Math.sin(-robotHeading);
        double rotY = strafe * Math.sin(-robotHeading) + drive * Math.cos(-robotHeading);

        // Use the rotated values for robot-centric drive
        drive(rotY, rotX, turn);
    }

    // ==================== MOTOR CONTROL ====================

    /**
     * Set individual motor powers directly
     */
    private void setPowers(double fl, double fr, double rl, double rr) {
        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        rearLeft.setPower(rl);
        rearRight.setPower(rr);
    }

    /**
     * Stop all motors immediately
     */
    public void stop() {
        setPowers(0, 0, 0, 0);
    }

    /**
     * Set power to all motors equally (for testing)
     * @param power Power level (-1.0 to 1.0)
     */
    public void setAllMotorPowers(double power) {
        setPowers(power, power, power, power);
    }

    // ==================== CONFIGURATION METHODS ====================

    /**
     * Set the joystick deadzone threshold
     * @param deadzone Deadzone value (0.0 to 1.0, typically 0.05-0.10)
     */
    public void setDeadzone(double deadzone) {
        this.deadzone = Math.max(0, Math.min(1.0, deadzone));
    }

    /**
     * Enable or disable precision mode (reduced speed for fine control)
     * @param enabled True to enable precision mode, false for normal speed
     */
    public void setPrecisionMode(boolean enabled) {
        speedScale = enabled ? PRECISION_SCALE : NORMAL_SCALE;
    }

    /**
     * Set a custom speed scale factor
     * @param scale Speed multiplier (0.0 to 1.0)
     */
    public void setSpeedScale(double scale) {
        this.speedScale = Math.max(0, Math.min(1.0, scale));
    }

    /**
     * Get the current speed scale
     * @return Current speed scale value
     */
    public double getSpeedScale() {
        return speedScale;
    }

    /**
     * Set zero power behavior for all motors
     * @param behavior BRAKE or FLOAT
     */
    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        frontLeft.setZeroPowerBehavior(behavior);
        frontRight.setZeroPowerBehavior(behavior);
        rearLeft.setZeroPowerBehavior(behavior);
        rearRight.setZeroPowerBehavior(behavior);
    }

    /**
     * Set run mode for all motors
     * @param mode RUN_WITHOUT_ENCODER, RUN_USING_ENCODER, RUN_TO_POSITION, or STOP_AND_RESET_ENCODER
     */
    public void setRunMode(DcMotor.RunMode mode) {
        frontLeft.setMode(mode);
        frontRight.setMode(mode);
        rearLeft.setMode(mode);
        rearRight.setMode(mode);
    }

    /**
     * Reset all motor encoders to zero
     */
    public void resetEncoders() {
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Apply deadzone filtering to an input value
     * @param value Input value
     * @return Filtered value (0 if within deadzone)
     */
    private double applyDeadzone(double value) {
        return Math.abs(value) < deadzone ? 0 : value;
    }

    /**
     * Get current encoder position for front left motor
     * @return Encoder ticks
     */
    public int getFrontLeftPosition() {
        return frontLeft.getCurrentPosition();
    }

    /**
     * Get current encoder position for front right motor
     * @return Encoder ticks
     */
    public int getFrontRightPosition() {
        return frontRight.getCurrentPosition();
    }

    /**
     * Get current encoder position for rear left motor
     * @return Encoder ticks
     */
    public int getRearLeftPosition() {
        return rearLeft.getCurrentPosition();
    }

    /**
     * Get current encoder position for rear right motor
     * @return Encoder ticks
     */
    public int getRearRightPosition() {
        return rearRight.getCurrentPosition();
    }

    /**
     * Get average encoder position across all motors
     * @return Average encoder ticks
     */
    public double getAverageEncoderPosition() {
        return (getFrontLeftPosition() + getFrontRightPosition() +
                getRearLeftPosition() + getRearRightPosition()) / 4.0;
    }

    /**
     * Check if all motors are busy (useful for RUN_TO_POSITION mode)
     * @return True if any motor is busy
     */
    public boolean isBusy() {
        return frontLeft.isBusy() || frontRight.isBusy() ||
                rearLeft.isBusy() || rearRight.isBusy();
    }

    // ==================== TELEMETRY ====================

    /**
     * Set telemetry object for debugging output
     * @param telemetry Telemetry object from OpMode
     */
    public void setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    /**
     * Send drivetrain status to telemetry
     */
    public void sendTelemetry() {
        if (telemetry == null) return;

        telemetry.addData("Drivetrain", "Mecanum");
        telemetry.addData("Speed Scale", "%.2f", speedScale);
        telemetry.addData("Deadzone", "%.2f", deadzone);
        telemetry.addData("FL Power", "%.2f", frontLeft.getPower());
        telemetry.addData("FR Power", "%.2f", frontRight.getPower());
        telemetry.addData("RL Power", "%.2f", rearLeft.getPower());
        telemetry.addData("RR Power", "%.2f", rearRight.getPower());
    }

    /**
     * Send encoder positions to telemetry
     */
    public void sendEncoderTelemetry() {
        if (telemetry == null) return;

        telemetry.addData("FL Encoder", getFrontLeftPosition());
        telemetry.addData("FR Encoder", getFrontRightPosition());
        telemetry.addData("RL Encoder", getRearLeftPosition());
        telemetry.addData("RR Encoder", getRearRightPosition());
        telemetry.addData("Avg Encoder", "%.1f", getAverageEncoderPosition());
    }

    // ==================== ADVANCED FEATURES ====================

    /**
     * Drive to a target position using encoders (blocks until complete)
     * @param targetTicks Target . position
     * @param power Motor power (0.0 to 1.0)
     * @param timeoutMs Maximum time to wait in milliseconds
     */
    public void driveToPosition(int targetTicks, double power, long timeoutMs) {
        // Set target positions
        frontLeft.setTargetPosition(targetTicks);
        frontRight.setTargetPosition(targetTicks);
        rearLeft.setTargetPosition(targetTicks);
        rearRight.setTargetPosition(targetTicks);

        // Switch to RUN_TO_POSITION mode
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);

        // Start moving
        setAllMotorPowers(Math.abs(power));

        // Wait until target reached or timeout
        long startTime = System.currentTimeMillis();
        while (isBusy() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            // Optional: Add telemetry here
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Stop motors
        stop();

        // Return to RUN_USING_ENCODER mode
        setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
}