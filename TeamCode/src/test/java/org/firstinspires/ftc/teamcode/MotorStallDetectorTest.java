
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

        // Motor moving normally
        for (int i = 0; i < 10; i++) {
            detector.update(0.3, 300, 0.1);
        }

        check("Normal motor operation",
                !detector.isStalled());

        // Short temporary blockage
        for (int i = 0; i < 3; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Short blockage ignored",
                !detector.isStalled());

        // Motor moves again
        detector.update(0.3, 300, 0.1);

        check("Stall timer reset",
                detector.getStallTimer() == 0);

        // Long blockage
        for (int i = 0; i < 7; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Long blockage detected",
                detector.isStalled());

        // Motor should remain locked
        detector.update(0, 300, 0.1);

        check("Motor stays locked",
                detector.isStalled());

        // Manual reset
        detector.reset();

        check("Manual reset",
                !detector.isStalled());

        // Reverse motor blockage
        for (int i = 0; i < 7; i++) {
            detector.update(-0.3, 0, 0.1);
        }

        check("Reverse blockage detected",
                detector.isStalled());

        System.out.println();
        System.out.println("ALL 8 SAFETY TESTS PASSED");
    }

    private static void check(String name, boolean passed) {

        if (!passed) {
            throw new AssertionError(name + " FAILED");
        }

        System.out.println(name + " PASSED");
    }
}
