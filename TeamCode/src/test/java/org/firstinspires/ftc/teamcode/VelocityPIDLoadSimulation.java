
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDLoadSimulation {

    public static void main(String[] args) throws IOException {

        // Adjusted PID parameters for this simulation
        VelocityPID pid = new VelocityPID(0.005, 0.01, 0);

        double velocity = 0;
        double target = 600;
        double maxVelocity = 1200;

        double deltaTime = 0.02;
        double responseTime = 0.35;

        double peakVelocity = 0;

        String file = "/tmp/ftc-pid-load.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power,Load");

            System.out.println("FTC PID LOAD SIMULATION");
            System.out.println("Time  Target  Velocity  Power  Load");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Resistance between 4 and 7 seconds
                boolean load = time >= 4 && time < 7;

                double efficiency = load ? 0.65 : 1.0;

                // PID correction
                double power = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                // Simulated motor response
                double requestedVelocity =
                        power * maxVelocity * efficiency;

                velocity += (requestedVelocity - velocity)
                        * deltaTime / responseTime;

                // Track overshoot after resistance is removed
                if (time >= 7) {
                    peakVelocity = Math.max(peakVelocity, velocity);
                }

                writer.printf(Locale.US,
                        "%.2f,%.0f,%.2f,%.3f,%s%n",
                        time, target, velocity, power, load
                );

                if (i % 25 == 0) {

                    System.out.printf(Locale.US,
                            "%4.1f   %4.0f    %7.1f   %5.2f   %s%n",
                            time, target, velocity, power,
                            load ? "ON" : "OFF"
                    );
                }
            }
        }

        double overshoot = (peakVelocity - target) / target * 100;

        System.out.printf(Locale.US,
                "%nPeak velocity: %.1f ticks/s%n",
                peakVelocity
        );

        System.out.printf(Locale.US,
                "Overshoot: %.2f%%%n",
                overshoot
        );

        System.out.println("Simulation completed!");
        System.out.println("Data saved to: " + file);
    }
}
