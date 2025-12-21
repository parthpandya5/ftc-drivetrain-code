package org.firstinspires.ftc.teamcode.util;

/**
 * Calculates required launch velocity for projectile motion
 * Used to determine flywheel speed based on distance to target
 */
public class BallisticsCalculator {

    // Physical constants
    private static final double GRAVITY = 9.81; // m/s^2

    // Robot/mechanism configuration (ADJUST THESE TO YOUR ROBOT)
    private static final double LAUNCHER_HEIGHT = 0.30; // meters (30cm, ~12 inches from ground)
    private static final double TARGET_HEIGHT = 0.91;   // meters (91cm, ~36 inches - typical high goal)
    private static final double LAUNCH_ANGLE = 45.0;    // degrees (optimal for range, adjust if angled differently)

    /**
     * Calculate required launch velocity to hit a target at given distance
     *
     * @param horizontalDistance Distance to target in meters
     * @return Required launch velocity in m/s
     */
    public static double calculateLaunchVelocity(double horizontalDistance) {
        double heightDifference = TARGET_HEIGHT - LAUNCHER_HEIGHT;
        double angleRadians = Math.toRadians(LAUNCH_ANGLE);

        // Projectile motion equation solved for initial velocity:
        // v = sqrt(g * d^2 / (2 * cos^2(θ) * (d * tan(θ) - h)))
        // where d = horizontal distance, θ = launch angle, h = height difference

        double cosAngle = Math.cos(angleRadians);
        double tanAngle = Math.tan(angleRadians);

        double numerator = GRAVITY * horizontalDistance * horizontalDistance;
        double denominator = 2 * cosAngle * cosAngle * (horizontalDistance * tanAngle - heightDifference);

        if (denominator <= 0) {
            // Target is unreachable with this angle
            return -1;
        }

        return Math.sqrt(numerator / denominator);
    }

    /**
     * Calculate required launch velocity using a custom launch angle
     *
     * @param horizontalDistance Distance to target in meters
     * @param customAngleDegrees Launch angle in degrees
     * @return Required launch velocity in m/s
     */
    public static double calculateLaunchVelocity(double horizontalDistance, double customAngleDegrees) {
        double heightDifference = TARGET_HEIGHT - LAUNCHER_HEIGHT;
        double angleRadians = Math.toRadians(customAngleDegrees);

        double cosAngle = Math.cos(angleRadians);
        double tanAngle = Math.tan(angleRadians);

        double numerator = GRAVITY * horizontalDistance * horizontalDistance;
        double denominator = 2 * cosAngle * cosAngle * (horizontalDistance * tanAngle - heightDifference);

        if (denominator <= 0) {
            return -1;
        }

        return Math.sqrt(numerator / denominator);
    }

    /**
     * Convert linear velocity (m/s) to flywheel RPM
     *
     * @param velocity Linear velocity in m/s
     * @param flywheelDiameter Flywheel diameter in meters
     * @return Required RPM
     */
    public static double velocityToRPM(double velocity, double flywheelDiameter) {
        // Circumference = π * diameter
        double circumference = Math.PI * flywheelDiameter;

        // RPM = (velocity / circumference) * 60 seconds
        return (velocity / circumference) * 60.0;
    }

    /**
     * Calculate distance to target from AprilTag data
     *
     * @param tx Horizontal offset in degrees (from Limelight)
     * @param ty Vertical offset in degrees (from Limelight)
     * @param cameraHeightFromGround Camera/Limelight height in meters
     * @param targetHeightFromGround Target AprilTag height in meters
     * @param cameraMountAngle Camera tilt angle in degrees (positive = tilted up)
     * @return Horizontal distance to target in meters
     */
    public static double calculateDistanceFromAprilTag(double tx, double ty,
                                                       double cameraHeightFromGround,
                                                       double targetHeightFromGround,
                                                       double cameraMountAngle) {
        // Height difference between camera and target
        double heightDifference = targetHeightFromGround - cameraHeightFromGround;

        // Angle to target = camera mount angle + ty offset
        double angleToTargetDegrees = cameraMountAngle + ty;
        double angleToTargetRadians = Math.toRadians(angleToTargetDegrees);

        // Distance = height difference / tan(angle)
        double distance = heightDifference / Math.tan(angleToTargetRadians);

        return Math.abs(distance);
    }

    /**
     * Convenience method to get RPM directly from distance
     *
     * @param horizontalDistance Distance to target in meters
     * @param flywheelDiameter Flywheel diameter in meters (e.g., 0.1 for 10cm)
     * @return Required motor RPM
     */
    public static double getRequiredRPM(double horizontalDistance, double flywheelDiameter) {
        double velocity = calculateLaunchVelocity(horizontalDistance);
        if (velocity < 0) {
            return -1; // Unreachable
        }
        return velocityToRPM(velocity, flywheelDiameter);
    }
}