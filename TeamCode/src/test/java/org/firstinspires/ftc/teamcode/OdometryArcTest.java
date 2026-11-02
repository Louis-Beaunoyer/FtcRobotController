
package org.firstinspires.ftc.teamcode;

public class OdometryArcTest {

    public static void main(String[] args) {

        // Example values for testing only
        double ticksPerRevolution = 1000;
        double wheelRadiusCm = 5.0 / Math.PI;

        Odometry odometry = new Odometry(
                ticksPerRevolution,
                wheelRadiusCm,
                0,
                0
        );

        odometry.reset(0, 0, 0);

        // Move forward 100 cm while rotating 90 degrees
        odometry.update(10000, 0, Math.PI / 2);

        // Expected position for a constant-speed arc
        double expected = 200.0 / Math.PI;

        double x = odometry.getXcm();
        double y = odometry.getYcm();
        double heading = odometry.getHeadingDegrees();

        System.out.println("ODOMETRY ARC TEST");
        System.out.println("X: " + x + " cm");
        System.out.println("Y: " + y + " cm");
        System.out.println("Heading: " + heading + " degrees");

        double tolerance = 0.01;

        if (!Double.isFinite(x) ||
                !Double.isFinite(y) ||
                !Double.isFinite(heading) ||
                Math.abs(x - expected) > tolerance ||
                Math.abs(y - expected) > tolerance ||
                Math.abs(heading - 90) > tolerance) {

            throw new AssertionError("ARC TEST FAILED");
        }

        System.out.println("ARC TEST PASSED");
    }
}
