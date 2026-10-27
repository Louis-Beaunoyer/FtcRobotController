
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

public class VelocityPIDTufnessSimulation {

    static double deltaTime = 0.02;
    static double maxVelocity = 1200;
    static double responseTime = 0.35;

    public static void main(String[] args) throws IOException {

        simulate("Load 20%", 20, false, "/tmp/ftc-pid-20.csv");
        simulate("Load 50%", 50, false, "/tmp/ftc-pid-50.csv");
        simulate("Load 70%", 70, false, "/tmp/ftc-pid-70.csv");

        simulate("Speed changes", 0, true,
                "/tmp/ftc-pid-speed.csv");

        System.out.println();
        System.out.println("ALL SIMULATIONS COMPLETED");
    }

    private static void simulate(
            String name,
            int loadPercent,
            boolean speedChanges,
            String file) throws IOException {

        VelocityPID pid = new VelocityPID(0.005, 0.01, 0);

        Random random = new Random(42);

        double velocity = 0;
        double peakVelocity = 0;
        double loadedSpeedTotal = 0;

        int loadedSamples = 0;
        int saturatedSamples = 0;

        System.out.println();
        System.out.println("TEST: " + name);
        System.out.println("Time  Target  Velocity  Power  Load");

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println(
                    "Time,Target,Velocity,Measured,Power,Load"
            );

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;
                double target = 600;

                // Different target speeds
                if (speedChanges) {

                    if (time < 3) {
                        target = 400;
                    } else if (time < 6) {
                        target = 800;
                    } else if (time < 9) {
                        target = 300;
                    } else {
                        target = 0;
                    }
                }

                // Apply load between 4 and 7 seconds
                double load = 0;

                if (!speedChanges && time >= 4 && time < 7) {
                    load = loadPercent / 100.0;
                }

                // Simulate encoder measurement noise
                double noise = random.nextGaussian() * 12;
                double measuredVelocity = velocity + noise;

                // PID correction
                double power = pid.calculate(
                        target,
                        measuredVelocity,
                        deltaTime
                );

                // Simulated motor response
                double requestedVelocity =
                        power * maxVelocity * (1 - load);

                velocity += (requestedVelocity - velocity)
                        * deltaTime / responseTime;

                // Count saturated motor commands
                if (Math.abs(power) >= 0.999) {
                    saturatedSamples++;
                }

                // Measure speed near the end of the load
                if (!speedChanges && time >= 6 && time < 7) {
                    loadedSpeedTotal += velocity;
                    loadedSamples++;
                }

                // Measure overshoot after load removal
                if (!speedChanges && time >= 7 && time < 9) {
                    peakVelocity = Math.max(peakVelocity, velocity);
                }

                // Save results
                writer.printf(Locale.US,
                        "%.2f,%.0f,%.2f,%.2f,%.3f,%.0f%n",
                        time,
                        target,
                        velocity,
                        measuredVelocity,
                        power,
                        load * 100
                );

                // Display results every second
                if (i % 50 == 0) {

                    System.out.printf(Locale.US,
                            "%4.1f   %4.0f    %7.1f   %5.2f   %.0f%%%n",
                            time,
                            target,
                            velocity,
                            power,
                            load * 100
                    );
                }
            }
        }

        System.out.printf(Locale.US,
                "Motor saturation: %.1f%%%n",
                saturatedSamples / 600.0 * 100
        );

        if (!speedChanges) {

            double averageSpeed =
                    loadedSpeedTotal / loadedSamples;

            double maxPossible =
                    maxVelocity * (1 - loadPercent / 100.0);

            double overshoot =
                    (peakVelocity - 600) / 600 * 100;

            System.out.printf(Locale.US,
                    "Maximum possible speed under load: %.1f ticks/s%n",
                    maxPossible
            );

            System.out.printf(Locale.US,
                    "Average speed under load: %.1f ticks/s%n",
                    averageSpeed
            );

            System.out.printf(Locale.US,
                    "Peak after load removal: %.1f ticks/s%n",
                    peakVelocity
            );

            System.out.printf(Locale.US,
                    "Overshoot: %.2f%%%n",
                    overshoot
            );

            if (maxPossible < 600) {
                System.out.println(
                        "WARNING: Target speed is physically unreachable in this model."
                );
            }

        } else {

            System.out.printf(Locale.US,
                    "Final velocity: %.2f ticks/s%n",
                    velocity
            );
        }

        System.out.println("Results saved to: " + file);
    }
}
