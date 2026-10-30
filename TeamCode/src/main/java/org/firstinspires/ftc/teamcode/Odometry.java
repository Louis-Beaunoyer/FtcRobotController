
package org.firstinspires.ftc.teamcode;

public class Odometry {

    private final double centimetersPerTick;
    private final double parallelOffsetCm;
    private final double perpendicularOffsetCm;

    private int previousParallelTicks;
    private int previousPerpendicularTicks;
    private double previousYawRadians;

    private double xCm;
    private double yCm;
    private double headingRadians;

    private boolean initialized = false;

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

    // X = forward, Y = left
    // Positive heading = counterclockwise

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

        // Convert encoder ticks to centimeters
        double deltaParallel =
                ((long) parallelTicks - previousParallelTicks)
                        * centimetersPerTick;

        double deltaPerpendicular =
                ((long) perpendicularTicks - previousPerpendicularTicks)
                        * centimetersPerTick;

        // Calculate the rotation since last update
        double deltaHeading = Math.atan2(
                Math.sin(yawRadians - previousYawRadians),
                Math.cos(yawRadians - previousYawRadians)
        );

        // Correct pod movement caused by robot rotation
        double forward =
                deltaParallel + parallelOffsetCm * deltaHeading;

        double left =
                deltaPerpendicular - perpendicularOffsetCm * deltaHeading;

        // Convert robot movement into coordinates
        double middleHeading =
                headingRadians + deltaHeading / 2.0;

        xCm += forward * Math.cos(middleHeading)
                - left * Math.sin(middleHeading);

        yCm += forward * Math.sin(middleHeading)
                + left * Math.cos(middleHeading);

        headingRadians += deltaHeading;

        // Save measure for the next update
        previousParallelTicks = parallelTicks;
        previousPerpendicularTicks = perpendicularTicks;
        previousYawRadians = yawRadians;
    }

    public double getXcm() {
        return xCm;
    }

    public double getYcm() {
        return yCm;
    }

    public double getHeadingDegrees() {
        return Math.toDegrees(headingRadians);
    }
}
