
package org.firstinspires.ftc.teamcode;

import java.util.Locale;

public class PIDStallIntegrationTest {

    public static void main(String[] args) {

        VelocityPID pid = new VelocityPID(0.005, 0.01, 0, 0.4);

        MotorStallDetector detector =
                new MotorStallDetector(0.2, 25, 0.6);

        double velocity = 0;
        double target = 300;
        double deltaTime = 0.02;
        double detectionTime = -1;
        double maxPower = 0;

        boolean remainedLocked = true;

        System.out.println("PID + STALL DETECTOR TEST");
        System.out.println("Time  Velocity  Power  Blocked  Safety");

        for (int i = 0; i < 600; i++) {

            double time = i * deltaTime;

            // Motor blocked between 4 and 7 seconds
            boolean blocked = time >= 4 && time < 7;

            if (blocked) {
                velocity = 0;
            }

            // Manual reset after the blockage is removed
            if (i == 400) {

                check("Motor locked before reset",
                        detector.isStalled());

                detector.reset();
                pid.reset();

                System.out.println("MANUAL RESET AT 8 SECONDS");
            }

            double power = 0;

            // PID controls the motor only when unlocked
            if (!detector.isStalled()) {

                double requestedPower = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                boolean stalled = detector.update(
                        requestedPower,
                        velocity,
                        deltaTime
                );

                if (stalled) {

                    if (detectionTime < 0) {
                        detectionTime = time;

                        System.out.printf(Locale.US,
                                "STALL DETECTED AT %.2f SECONDS%n",
                                time
                        );
                    }

                } else {
                    power = requestedPower;
                }
            }

            // A locked motor must never receive power
            if (detector.isStalled() &&
                    Math.abs(power) > 0.000001) {

                throw new AssertionError(
                        "Locked motor received power"
                );
            }

            // Motor must stay stopped until manual reset
            if (time >= 7 && time < 8 &&
                    Math.abs(power) > 0.000001) {

                remainedLocked = false;
            }

            maxPower = Math.max(maxPower, Math.abs(power));

            // Simulated motor movement
            if (!blocked) {

                double requestedVelocity = power * 1200;

                velocity += (requestedVelocity - velocity)
                        * deltaTime / 0.35;
            }

            if (i % 25 == 0) {

                System.out.printf(Locale.US,
                        "%4.1f   %7.1f   %5.2f   %s   %s%n",
                        time,
                        velocity,
                        power,
                        blocked ? "YES" : "NO",
                        detector.isStalled() ? "LOCKED" : "ENABLED"
                );
            }
        }

        // Final checks
        check("Stall detected",
                detectionTime >= 4.5 && detectionTime <= 4.7);

        check("Power never above 40%",
                maxPower <= 0.400001);

        check("No restart before reset",
                remainedLocked);

        check("Motor recovered",
                Math.abs(velocity - target) <= 15);

        System.out.println();
        System.out.println("ALL INTEGRATION TESTS PASSED");
    }

    private static void check(String name, boolean passed) {

        if (!passed) {
            throw new AssertionError(name + " FAILED");
        }

        System.out.println(name + " PASSED");
    }
}
