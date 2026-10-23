
package org.firstinspires.ftc.teamcode;

public class MotorStallDetectorTest {

    public static void main(String[] args) {

        MotorStallDetector detector =
                new MotorStallDetector(0.2, 25, 0.6);

        // Motor stopped without power
        for (int i = 0; i < 10; i++) {
            detector.update(0, 0, 0.1);
        }

        check("No false alarm without power",
                !detector.isStalled());

        // Normal motor operation
        for (int i = 0; i < 10; i++) {
            detector.update(0.3, 300, 0.1);
        }

        check("Normal motor operation",
                !detector.isStalled());

        // Short blockage
        for (int i = 0; i < 3; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Short blockage ignored",
                !detector.isStalled());

        detector.update(0.3, 300, 0.1);

        check("Stall timer reset",
                detector.getStallTimer() == 0);

        // Long blockage
        for (int i = 0; i < 7; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Long blockage detected",
                detector.isStalled());

        detector.update(0, 300, 0.1);

        check("Motor stays locked",
                detector.isStalled());

        detector.reset();

        check("Manual reset",
                !detector.isStalled());

        // Reverse blockage
        for (int i = 0; i < 7; i++) {
            detector.update(-0.3, 0, 0.1);
        }

        check("Reverse blockage detected",
                detector.isStalled());

        // Invalid velocity
        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("NaN velocity locks",
                detector.update(0.3, Double.NaN, 0.1));

        check("Sensor fault reported",
                detector.hasSensorFault());

        check("Fault stays locked",
                detector.update(0, 300, 0.1));

        // Manual reset after sensor fault
        detector.reset();

        check("Reset clears lock",
                !detector.isStalled());

        check("Reset clears fault",
                !detector.hasSensorFault());

        // Infinite velocity
        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("Infinite velocity locks",
                detector.update(
                        0.3, Double.POSITIVE_INFINITY, 0.1));

        // Invalid motor power
        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("NaN power locks",
                detector.update(Double.NaN, 300, 0.1));

        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("Infinite power locks",
                detector.update(
                        Double.NEGATIVE_INFINITY, 300, 0.1));

        // Invalid time intervals
        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("Zero deltaTime locks",
                detector.update(0.3, 300, 0));

        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("Negative deltaTime locks",
                detector.update(0.3, 300, -0.1));

        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("NaN deltaTime locks",
                detector.update(0.3, 300, Double.NaN));

        detector = new MotorStallDetector(0.2, 25, 0.6);

        check("Infinite deltaTime locks",
                detector.update(
                        0.3, 300, Double.POSITIVE_INFINITY));

        System.out.println();
        System.out.println("ALL 20 SAFETY TESTS PASSED");
    }

    private static void check(String name, boolean passed) {

        if (!passed) {
            throw new AssertionError(name + " FAILED");
        }

        System.out.println(name + " PASSED");
    }
}
