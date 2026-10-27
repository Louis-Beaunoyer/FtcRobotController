
package org.firstinspires.ftc.teamcode;

public class MotorStallEdgeCaseTest {

    public static void main(String[] args) {

        MotorStallDetector detector;

        // No power: no stall
        detector = new MotorStallDetector(0.2, 25, 0.6);

        for (int i = 0; i < 10; i++) {
            detector.update(0, 0, 0.1);
        }

        check("No power", !detector.isStalled());

        // Motor moving normally
        detector = new MotorStallDetector(0.2, 25, 0.6);

        for (int i = 0; i < 10; i++) {
            detector.update(0.3, 150, 0.1);
        }

        check("Normal movement", !detector.isStalled());

        // Short blockage: less than 0.6 seconds
        detector = new MotorStallDetector(0.2, 25, 0.6);

        for (int i = 0; i < 5; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Short blockage ignored", !detector.isStalled());

        // Movement resumes and resets the timer
        detector.update(0.3, 150, 0.1);

        check("Timer reset", detector.getStallTimer() == 0);

        // Variable time intervals
        detector = new MotorStallDetector(0.2, 25, 0.6);

        detector.update(0.3, 0, 0.15);
        detector.update(0.3, 0, 0.20);

        check("Variable timing before timeout",
                !detector.isStalled());

        detector.update(0.3, 0, 0.30);

        check("Variable timing detection",
                detector.isStalled());

        // Reverse movement
        detector = new MotorStallDetector(0.2, 25, 0.6);

        detector.update(-0.3, -150, 0.1);

        check("Reverse movement", !detector.isStalled());

        // Power below the detection threshold
        detector = new MotorStallDetector(0.2, 25, 0.6);

        for (int i = 0; i < 10; i++) {
            detector.update(0.1, 0, 0.1);
        }

        check("Low power ignored", !detector.isStalled());

        // An encoder stuck at zero looks like a stalled motor
        detector = new MotorStallDetector(0.2, 25, 0.6);

        for (int i = 0; i < 7; i++) {
            detector.update(0.3, 0, 0.1);
        }

        check("Zero velocity detected",
                detector.isStalled());

        check("Zero velocity not identified as sensor fault",
                !detector.hasSensorFault());

        // The lock remains active even if velocity returns
        detector.update(0, 150, 0.1);

        check("Lock remains active", detector.isStalled());

        // Manual reset
        detector.reset();

        check("Manual reset works", !detector.isStalled());

        // Invalid encoder reading
        detector.update(0.3, Double.NaN, 0.1);

        check("Invalid reading locks", detector.isStalled());

        check("Sensor fault recorded", detector.hasSensorFault());

        System.out.println();
        System.out.println("ALL 14 EDGE CASE TESTS PASSED");
    }

    private static void check(String name, boolean passed) {

        if (!passed) {
            throw new AssertionError(name + " FAILED");
        }

        System.out.println(name + " PASSED");
    }
}
