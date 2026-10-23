
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDSimulation {

    public static void main(String[] args) throws IOException {

        VelocityPID pid = new VelocityPID(0.001, 0.003, 0);

        double velocity = 0;
        double maxVelocity = 1200;
        double responseTime = 0.35;
        double deltaTime = 0.02;

        String file = "/tmp/ftc-pid-simulation.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power");

            System.out.println("FTC PID MOTOR SIMULATION");
            System.out.println("Time   Target   Velocity   Power   Graph");

            for (int i = 0; i < 500; i++) {

                double time = i * deltaTime;

                // Change the target speed during the simulation
                double target;

                if (time < 4) {
                    target = 500;
                } else if (time < 7) {
                    target = 800;
                } else {
                    target = 0;
                }

                // PID calculates the motor power
                double power = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                // Simulate the motor response
                double requestedVelocity = power * maxVelocity;

                velocity += (requestedVelocity - velocity)
                        * deltaTime / responseTime;

                // Save data for a future graph
                writer.printf(Locale.US,
                        "%.2f,%.2f,%.2f,%.3f%n",
                        time, target, velocity, power
                );

                // Show results every 0.5 seconds
                if (i % 25 == 0) {

                    int bars = (int) Math.round(
                            Math.abs(velocity) / 50
                    );

                    String graph = "#".repeat(
                            Math.min(bars, 24)
                    );

                    System.out.printf(Locale.US,
                            "%4.1f   %5.0f    %7.1f   %5.2f   %s%n",
                            time, target, velocity, power, graph
                    );
                }
            }
        }

        System.out.println();
        System.out.println("Simulation completed!");
        System.out.println("Data saved to: " + file);
    }
}
