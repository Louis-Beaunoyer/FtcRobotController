
package org.firstinspires.ftc.teamcode;

public class Odometry {

    // Converts encoder ticks in centimeters
    private final double centimetersPerTick;

    // Signed distance of each odopod from the robot reference point
    private final double parallelOffsetCm;
    private final double perpendicularOffsetCm;

    // Previous sensor readings used to calculate movement
    private int previousParallelTicks;
    private int previousPerpendicularTicks;
    private double previousYawRadians;

    // Robot position relative to the last reset
    private double xCm;
    private double yCm;
    private double headingRadians;

    private boolean initialized = false;

    // Creates the odometry calculator with the wheel dimensions
    // and the positions of the two odopods
    public Odometry(double ticksPerRevolution,
                    double wheelRadiusCm,
                    double parallelOffsetCm,
                    double perpendicularOffsetCm) {

        if (!Double.isFinite(ticksPerRevolution) ||
                ticksPerRevolution <= 0 ||
                !Double.isFinite(wheelRadiusCm) ||
                wheelRadiusCm <= 0 ||
                !Double.isFinite(parallelOffsetCm) ||
                !Double.isFinite(perpendicularOffsetCm)) {

            throw new IllegalArgumentException(
                    "Invalid odometry parameters");
        }

        centimetersPerTick =
                2 * Math.PI * wheelRadiusCm / ticksPerRevolution;

        this.parallelOffsetCm = parallelOffsetCm;
        this.perpendicularOffsetCm = perpendicularOffsetCm;
    }

    // Coordinate system:
    // X is forward and Y is left
    // Positive is counterclockwise.
    // Parallel offset is positive on the left side
    // Perpendicular offset is positive toward the front

    // Sets the current sensor as the starting point
    // This does not reset the physical encoders or IMU
    public void reset(int parallelTicks,
                      int perpendicularTicks,
                      double yawRadians) {

        if (!Double.isFinite(yawRadians)) {
            throw new IllegalArgumentException("Invalid IMU angle");
        }

        previousParallelTicks = parallelTicks;
        previousPerpendicularTicks = perpendicularTicks;
        previousYawRadians = yawRadians;

        xCm = 0;
        yCm = 0;
        headingRadians = 0;

        initialized = true;
    }

    // Updates the robot position using both odopods and the IMU
    // This method should be called once per robot update cycle normaly
    public void update(int parallelTicks,
                       int perpendicularTicks,
                       double yawRadians) {

        if (!Double.isFinite(yawRadians)) {
            throw new IllegalArgumentException("Invalid IMU angle");
        }

        if (!initialized) {
            reset(parallelTicks, perpendicularTicks, yawRadians);
            return;
        }

        // Step 1: Convert encoder changes in centimeters.
        double deltaParallel =
                ((long) parallelTicks - previousParallelTicks)
                        * centimetersPerTick;

        double deltaPerpendicular =
                ((long) perpendicularTicks - previousPerpendicularTicks)
                        * centimetersPerTick;

        // Step 2: Calculate the change in heading
        // atan2 handles the transition entre +180 and -180 degrees
        double deltaHeading = Math.atan2(
                Math.sin(yawRadians - previousYawRadians),
                Math.cos(yawRadians - previousYawRadians)
        );

        // Step 3: Remove the odopod movement caused by rotation
        double forward =
                deltaParallel + parallelOffsetCm * deltaHeading;

        double left =
                deltaPerpendicular - perpendicularOffsetCm * deltaHeading;

        // Step 4: Correct the distance when the robot
        // moves and rotates in the same time
        double halfTurn = deltaHeading / 2.0;
        double arcScale;

        // Use an approximation near zero to avoid division by zero
        if (Math.abs(halfTurn) < 0.0001) {
            double square = halfTurn * halfTurn;

            arcScale = 1.0 - square / 6.0
                    + square * square / 120.0;
        } else {
            arcScale = Math.sin(halfTurn) / halfTurn;
        }

        // Step 5: Convert local robot movement into field coordinates
        double middleHeading = headingRadians + halfTurn;

        xCm += arcScale * (
                forward * Math.cos(middleHeading)
                        - left * Math.sin(middleHeading)
        );

        yCm += arcScale * (
                forward * Math.sin(middleHeading)
                        + left * Math.cos(middleHeading)
        );

        headingRadians += deltaHeading;

        // Step 6: Save the readings for the next update.
        previousParallelTicks = parallelTicks;
        previousPerpendicularTicks = perpendicularTicks;
        previousYawRadians = yawRadians;
    }

    // Returns the robot position in centimeters
    public double getXcm() {
        return xCm;
    }

    public double getYcm() {
        return yCm;
    }

    // Returns the accumuled heading in degrees
    public double getHeadingDegrees() {
        return Math.toDegrees(headingRadians);
    }
}
