
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDStallSimulation {

    public static void main(String[] args) throws IOException {

        VelocityPID pid = new VelocityPID(0.005, 0.01, 0, 0.4);

        double velocity = 0;
        double target = 300;

        double maxVelocity = 1200;
        double deltaTime = 0.02;
        double responseTime = 0.35;

        double peakVelocity = 0;
        double maxBlockedPower = 0;

        double stableDuration = 0;
        double recoveryTime = -1;

        String file = "/tmp/ftc-pid-stall.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power,Blocked");

            System.out.println("FTC PID STALL SIMULATION");
            System.out.println("Time  Target  Velocity  Power  Blocked");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Motor blocked between 4 and 7 seconds
                boolean blocked = time >= 4 && time < 7;

                double power = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                // Simulate motor behavior
                if (blocked) {

                    velocity = 0;

                    maxBlockedPower = Math.max(
                            maxBlockedPower,
                            Math.abs(power)
                    );

                } else {

                    double requestedVelocity = power * maxVelocity;

                    velocity += (requestedVelocity - velocity)
                            * deltaTime / responseTime;
                }

                // Measure recovery after the motor is released
                if (time >= 7) {

                    peakVelocity = Math.max(peakVelocity, velocity);

                    if (Math.abs(velocity - target) <= target * 0.05) {
                        stableDuration += deltaTime;
                    } else {
                        stableDuration = 0;
                    }

                    if (stableDuration >= 0.5 && recoveryTime < 0) {
                        recoveryTime = time - 7;
                    }
                }

                writer.printf(Locale.US,
                        "%.2f,%.0f,%.2f,%.3f,%s%n",
                        time, target, velocity, power, blocked
                );

                if (i % 25 == 0) {

                    System.out.printf(Locale.US,
                            "%4.1f   %4.0f    %7.1f   %5.2f   %s%n",
                            time, target, velocity, power,
                            blocked ? "YES" : "NO"
                    );
                }
            }
        }

        System.out.println();

        System.out.printf(Locale.US,
                "Maximum power during stall: %.2f%n",
                maxBlockedPower
        );

        System.out.printf(Locale.US,
                "Peak velocity after release: %.1f ticks/s%n",
                peakVelocity
        );

        if (recoveryTime >= 0) {

            System.out.printf(Locale.US,
                    "Recovery time: %.2f seconds%n",
                    recoveryTime
            );

        } else {

            System.out.println("Motor did not stabilize.");
        }

        System.out.println("Simulation completed!");
        System.out.println("Data saved to: " + file);
    }
}
