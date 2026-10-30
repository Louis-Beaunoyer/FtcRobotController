
package org.firstinspires.ftc.teamcode;

public class OdometrySimulation {

    // Exemple values used only for simulation
    static final double TICKS_PER_REV = 1000;
    static final double WHEEL_RADIUS_CM = 5.0 / Math.PI;

    static final double PARALLEL_OFFSET_CM = 10;
    static final double PERPENDICULAR_OFFSET_CM = 15;

    static final double TICKS_PER_CM =
            TICKS_PER_REV / (2 * Math.PI * WHEEL_RADIUS_CM);

    public static void main(String[] args) {

        System.out.println("ODOMETRY SIMULATION");
        System.out.println();

        // Teste 1 Move front 100 cm
        Odometry odo = createOdometry();

        odo.reset(0, 0, 0);
        odo.update(10000, 0, 0);

        check("Forward 100 cm", odo, 100, 0, 0);

        // Teste 2 Move left 50 cm
        odo = createOdometry();

        odo.reset(0, 0, 0);
        odo.update(0, 5000, 0);

        check("Left 50 cm", odo, 0, 50, 0);

        // Simulate a 90 degree rotation
        double rotation = Math.PI / 2;

        int parallelRotationTicks = (int) Math.round(
                -PARALLEL_OFFSET_CM * rotation * TICKS_PER_CM
        );

        int perpendicularRotationTicks = (int) Math.round(
                PERPENDICULAR_OFFSET_CM * rotation * TICKS_PER_CM
        );

        // Teste 3 Rotate without changing position
        odo = createOdometry();

        odo.reset(0, 0, 0);

        odo.update(
                parallelRotationTicks,
                perpendicularRotationTicks,
                rotation
        );

        check("Rotate 90 degrees", odo, 0, 0, 90);

        // Teste 4: Rotate, then move 100 cm front
        odo = createOdometry();

        odo.reset(0, 0, 0);

        odo.update(
                parallelRotationTicks,
                perpendicularRotationTicks,
                rotation
        );

        odo.update(
                parallelRotationTicks + 10000,
                perpendicularRotationTicks,
                rotation
        );

        check("Forward after rotation", odo, 0, 100, 90);

        // Teste 5 Reset the position
        odo.reset(100, 200, rotation);

        check("Reset position", odo, 0, 0, 0);

        System.out.println();
        System.out.println("ALL ODOMETRY TESTS PASSED");
    }

    private static Odometry createOdometry() {

        return new Odometry(
                TICKS_PER_REV,
                WHEEL_RADIUS_CM,
                PARALLEL_OFFSET_CM,
                PERPENDICULAR_OFFSET_CM
        );
    }

    private static void check(
            String name,
            Odometry odo,
            double expectedX,
            double expectedY,
            double expectedHeading) {

        double tolerance = 0.05;

        boolean passed =
                Math.abs(odo.getXcm() - expectedX) < tolerance &&
                        Math.abs(odo.getYcm() - expectedY) < tolerance &&
                        Math.abs(odo.getHeadingDegrees() - expectedHeading) < tolerance;

        if (!passed) {

            throw new AssertionError(
                    name + " FAILED: X=" + odo.getXcm() +
                            ", Y=" + odo.getYcm() +
                            ", Heading=" + odo.getHeadingDegrees()
            );
        }

        System.out.println(name + " PASSED");
    }
}
