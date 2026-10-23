
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDLoadSimulation {

    public static void main(String[] args) throws IOException {

        VelocityPID pid = new VelocityPID(0.001, 0.003, 0);

        double velocity = 0;
        double target = 600;
        double maxVelocity = 1200;

        double deltaTime = 0.02;
        double responseTime = 0.35;

        String file = "/tmp/ftc-pid-load.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power,Load");

            System.out.println("FTC PID LOAD SIMULATION");
            System.out.println("Time  Target  Velocity  Power  Load");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Simulate additional resistance
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

        System.out.println("Simulation completed!");
        System.out.println("Data saved to: " + file);
    }
}
