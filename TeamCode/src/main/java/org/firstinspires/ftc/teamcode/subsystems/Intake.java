package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Subsystem for controlling an active intake mechanism
 */
public class Intake {

    // Hardware
    private final DcMotor intakeMotor;

    // Configuration constants
    private static final double INTAKE_POWER = 1.0;
    private static final double OUTTAKE_POWER = -1.0;

    // State
    private boolean isRunning = false;

    /**
     * Initialize the intake subsystem
     *
     * @param hardwareMap The OpMode's hardware map
     * @param motorName   The name of the motor in the Robot Configuration (e.g.,
     *                    "intake")
     */
    public Intake(HardwareMap hardwareMap, String motorName) {
        try {
            intakeMotor = hardwareMap.get(DcMotor.class, motorName);
            // Default to running without encoder since intake speed doesn't need precise
            // PID
            intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // Coast when stopped
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Intake motor '" + motorName + "' not found in robot configuration. " +
                            "Check that motor is configured in Driver Station with this exact name.",
                    e);
        }
    }

    /**
     * Run the intake to pull game elements in
     */
    public void in() {
        intakeMotor.setPower(INTAKE_POWER);
        isRunning = true;
    }

    /**
     * Run the intake in reverse to eject game elements or un-jam
     */
    public void out() {
        intakeMotor.setPower(OUTTAKE_POWER);
        isRunning = true;
    }

    /**
     * Stop the intake
     */
    public void stop() {
        intakeMotor.setPower(0.0);
        isRunning = false;
    }

    /**
     * Set a custom power level (useful for variable speed control via triggers)
     *
     * @param power Power level from -1.0 to 1.0
     */
    public void setPower(double power) {
        intakeMotor.setPower(power);
        isRunning = Math.abs(power) > 0.05;
    }

    /**
     * Get current power
     */
    public double getPower() {
        return intakeMotor.getPower();
    }

    /**
     * Check if intake is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
