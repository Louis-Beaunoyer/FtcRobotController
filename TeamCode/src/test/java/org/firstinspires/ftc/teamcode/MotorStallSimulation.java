
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class MotorStallSimulation {

    public static void main(String[] args) throws IOException {

        VelocityPID pid = new VelocityPID(0.005, 0.01, 0, 0.4);

        MotorStallDetector detector =
                new MotorStallDetector(0.2, 25, 0.6);

        double target = 300;
        double velocity = 0;
        double deltaTime = 0.02;
        double maxVelocity = 1200;
        double responseTime = 0.35;

        double detectionTime = -1;
        double recoveryVelocity = 0;

        String file = "/tmp/ftc-motor-stall-safety.csv";

        System.out.println("FTC MOTOR STALL SAFETY TEST");
        System.out.println("Time  Velocity  Power  Blocked  Safety");

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Velocity,Power,Blocked,Safety");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Motor physically blocked from 4 to 7 seconds
                boolean blocked = time >= 4 && time < 7;

                if (blocked) {
                    velocity = 0;
                }

                // Simulated manual reset at 8 seconds
                if (i == 400) {

                    if (!detector.isStalled()) {
                        throw new AssertionError(
                                "Motor should still be locked"
                        );
                    }

                    detector.reset();
                    pid.reset();

                    System.out.println("MANUAL RESET AT 8 SECONDS");
                }

                double power = 0;

                if (!detector.isStalled()) {

                    power = pid.calculate(
                            target,
                            velocity,
                            deltaTime
                    );

                    // Check motor stall
                    boolean stalled = detector.update(
                            power,
                            velocity,
                            deltaTime
                    );

                    if (stalled) {
                        power = 0;

                        detectionTime = time;

                        System.out.printf(Locale.US,
                                "STALL DETECTED AT %.2f SECONDS%n",
                                time
                        );
                    }
                }

                // Software safety: never power a locked motor
                if (detector.isStalled() && power != 0) {
                    throw new AssertionError(
                            "Motor is powered while locked!"
                    );
                }

                // Check that motor cannot restart automatically
                if (time >= 7 && time < 8 && power != 0) {
                    throw new AssertionError(
                            "Motor restarted without manual reset!"
                    );
                }

                // Simulate motor movement
                if (!blocked) {

                    double requestedVelocity = power * maxVelocity;

                    velocity += (requestedVelocity - velocity)
                            * deltaTime / responseTime;
                }

                String safety = detector.isStalled()
                        ? "LOCKED" : "ENABLED";

                writer.printf(Locale.US,
                        "%.2f,%.2f,%.3f,%s,%s%n",
                        time, velocity, power, blocked, safety
                );

                if (i % 25 == 0) {

                    System.out.printf(Locale.US,
                            "%4.1f   %7.1f   %5.2f   %s   %s%n",
                            time,
                            velocity,
                            power,
                            blocked ? "YES" : "NO",
                            safety
                    );
                }

                recoveryVelocity = velocity;
            }
        }

        if (detectionTime < 4.5 || detectionTime > 4.7) {
            throw new AssertionError(
                    "Stall detection time is incorrect"
            );
        }

        if (Math.abs(recoveryVelocity - target) > 15) {
            throw new AssertionError(
                    "Motor did not recover after manual reset"
            );
        }

        System.out.println();
        System.out.printf(Locale.US,
                "Stall detection time: %.2f seconds%n",
                detectionTime
        );

        System.out.printf(Locale.US,
                "Final velocity: %.1f ticks/s%n",
                recoveryVelocity
        );

        System.out.println("Automatic stop: PASSED");
        System.out.println("Motor lock: PASSED");
        System.out.println("Manual reset: PASSED");
        System.out.println("Recovery: PASSED");

        System.out.println("ALL SAFETY TESTS PASSED");
        System.out.println("Results saved to: " + file);
    }
}
