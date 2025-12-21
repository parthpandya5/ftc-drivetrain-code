package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.BallisticsCalculator;

/**
 * Subsystem for controlling a flywheel launcher mechanism
 * Supports velocity control for consistent launch speeds
 */
public class FlywheelLauncher {

    // Hardware
    private DcMotorEx flywheelMotor;

    // Configuration constants (ADJUST THESE TO YOUR SETUP)
    private static final double FLYWHEEL_DIAMETER = 0.1; // meters (10cm / ~4 inches)
    private static final double TICKS_PER_REVOLUTION = 28.0; // REV HD Hex motor
    // If using goBILDA Yellow Jacket, use appropriate TPR (e.g., 537.7 for 5203-2402-0019)
    // If using REV Core Hex, use 288

    private static final double GEAR_RATIO = 1.0; // Adjust if you have gearing

    // Velocity PID coefficients (TUNE THESE)
    private static final double kP = 15.0;  // Proportional gain
    private static final double kI = 0.1;   // Integral gain
    private static final double kD = 0.0;   // Derivative gain
    private static final double kF = 12.5;  // Feedforward gain

    // State variables
    private double targetRPM = 0;
    private boolean isSpinningUp = false;
    private ElapsedTime spinUpTimer;

    /**
     * Initialize the flywheel launcher
     *
     * @param hardwareMap The hardware map from the OpMode
     * @param motorName Name of the flywheel motor in robot configuration
     * @throws IllegalArgumentException if motor not found in configuration
     */
    public FlywheelLauncher(HardwareMap hardwareMap, String motorName) {
        try {
            // Initialize motor
            flywheelMotor = hardwareMap.get(DcMotorEx.class, motorName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Flywheel motor '" + motorName + "' not found in robot configuration. " +
                            "Check that motor is configured in Driver Station with this exact name.", e);
        }

        // Configure motor
        flywheelMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // Coast for flywheel

        // Set velocity PID coefficients
        flywheelMotor.setVelocityPIDFCoefficients(kP, kI, kD, kF);

        spinUpTimer = new ElapsedTime();
    }

    /**
     * Set flywheel speed based on distance to target
     *
     * @param distanceMeters Horizontal distance to target in meters
     */
    public void setSpeedForDistance(double distanceMeters) {
        double requiredRPM = BallisticsCalculator.getRequiredRPM(distanceMeters, FLYWHEEL_DIAMETER);

        if (requiredRPM < 0) {
            // Target unreachable
            targetRPM = 0;
        } else {
            targetRPM = requiredRPM;
            setTargetRPM(requiredRPM);
        }
    }

    /**
     * Set flywheel to a specific RPM
     *
     * @param rpm Target RPM
     */
    public void setTargetRPM(double rpm) {
        this.targetRPM = rpm;

        // Convert RPM to ticks per second for the motor controller
        // ticks/sec = (RPM / 60) * ticks_per_rev * gear_ratio
        double ticksPerSecond = (rpm / 60.0) * TICKS_PER_REVOLUTION * GEAR_RATIO;

        flywheelMotor.setVelocity(ticksPerSecond);

        isSpinningUp = true;
        spinUpTimer.reset();
    }

    /**
     * Set flywheel power directly (0.0 to 1.0)
     * Use this for manual control or testing
     *
     * @param power Motor power (-1.0 to 1.0)
     */
    public void setPower(double power) {
        flywheelMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flywheelMotor.setPower(power);
        isSpinningUp = false;
    }

    /**
     * Stop the flywheel
     */
    public void stop() {
        flywheelMotor.setVelocity(0);
        targetRPM = 0;
        isSpinningUp = false;
    }

    /**
     * Get current flywheel RPM
     *
     * @return Current RPM
     */
    public double getCurrentRPM() {
        double ticksPerSecond = flywheelMotor.getVelocity();
        return (ticksPerSecond * 60.0) / (TICKS_PER_REVOLUTION * GEAR_RATIO);
    }

    /**
     * Check if flywheel is at target speed (within tolerance)
     *
     * @param toleranceRPM Acceptable RPM difference (e.g., 50 RPM)
     * @return True if at speed
     */
    public boolean isAtSpeed(double toleranceRPM) {
        if (!isSpinningUp) return false;

        double currentRPM = getCurrentRPM();
        return Math.abs(currentRPM - targetRPM) < toleranceRPM;
    }

    /**
     * Check if flywheel is ready to shoot (at speed for minimum time)
     *
     * @param toleranceRPM Acceptable RPM difference
     * @param minTimeSeconds Minimum time at speed (seconds)
     * @return True if ready
     */
    public boolean isReadyToShoot(double toleranceRPM, double minTimeSeconds) {
        return isAtSpeed(toleranceRPM) && spinUpTimer.seconds() >= minTimeSeconds;
    }

    /**
     * Get target RPM
     */
    public double getTargetRPM() {
        return targetRPM;
    }

    /**
     * Get motor power for debugging
     */
    public double getMotorPower() {
        return flywheelMotor.getPower();
    }

    /**
     * Get error (difference between target and current RPM)
     */
    public double getError() {
        return targetRPM - getCurrentRPM();
    }
}