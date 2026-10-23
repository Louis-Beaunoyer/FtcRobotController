
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDPowerLimitSimulation {

    static double powerLimit = 0.4;
    static double maxVelocity = 1200;
    static double deltaTime = 0.02;
    static double responseTime = 0.35;

    public static void main(String[] args) throws IOException {

        simulate(0);
        simulate(20);
        simulate(50);
        simulate(70);

        System.out.println();
        System.out.println("ALL SIMULATIONS COMPLETED");
    }

    private static void simulate(int loadPercent) throws IOException {

        VelocityPID pid = new VelocityPID(0.005, 0.01, 0);

        double target = 300;
        double velocity = 0;

        double totalVelocity = 0;
        int samples = 0;
        int saturationCount = 0;

        String file = "/tmp/ftc-pid-limit-" + loadPercent + ".csv";

        System.out.println();
        System.out.println("TEST: Load " + loadPercent + "%");
        System.out.println("Time  Target  Velocity  Power  Load");

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power,Load");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Apply resistance between 4 and 7 seconds
                double load = 0;

                if (time >= 4 && time < 7) {
                    load = loadPercent / 100.0;
                }

                // Calculate PID output
                double pidPower = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                // Limit motor power to 40%
                double power = Math.max(
                        -powerLimit,
                        Math.min(powerLimit, pidPower)
                );

                if (Math.abs(pidPower) > powerLimit) {
                    saturationCount++;
                }

                // Simulate motor movement
                double requestedVelocity =
                        power * maxVelocity * (1 - load);

                velocity += (requestedVelocity - velocity)
                        * deltaTime / responseTime;

                // Average speed during the last second of load
                if (time >= 6 && time < 7) {
                    totalVelocity += velocity;
                    samples++;
                }

                writer.printf(Locale.US,
                        "%.2f,%.0f,%.2f,%.3f,%.0f%n",
                        time, target, velocity, power, load * 100
                );

                if (i % 50 == 0) {

                    System.out.printf(Locale.US,
                            "%4.1f   %4.0f    %7.1f   %5.2f   %.0f%%%n",
                            time, target, velocity, power, load * 100
                    );
                }
            }
        }

        double maximumPossible =
                maxVelocity * powerLimit * (1 - loadPercent / 100.0);

        double averageVelocity = totalVelocity / samples;

        System.out.printf(Locale.US,
                "Maximum speed under load: %.1f ticks/s%n",
                maximumPossible
        );

        System.out.printf(Locale.US,
                "Average speed under load: %.1f ticks/s%n",
                averageVelocity
        );

        System.out.printf(Locale.US,
                "Power saturation: %.1f%%%n",
                saturationCount / 600.0 * 100
        );

        if (maximumPossible < target) {
            System.out.println("WARNING: Target speed is unreachable.");
        }

        System.out.println("Results saved to: " + file);
    }
}
