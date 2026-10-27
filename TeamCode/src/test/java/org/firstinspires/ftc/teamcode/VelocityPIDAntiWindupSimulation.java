
package org.firstinspires.ftc.teamcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class VelocityPIDAntiWindupSimulation {

    static double powerLimit = 0.4;
    static double maxVelocity = 1200;
    static double responseTime = 0.35;
    static double deltaTime = 0.02;
    static double target = 300;

    public static void main(String[] args) throws IOException {

        System.out.println("FTC PID ANTI-WINDUP COMPARISON");

        compare(20);
        compare(50);
        compare(70);

        System.out.println();
        System.out.println("ALL SIMULATIONS COMPLETED");
    }

    private static void compare(int loadPercent) throws IOException {

        Result oldPID = simulate(loadPercent, false);
        Result newPID = simulate(loadPercent, true);

        System.out.println();
        System.out.println("LOAD: " + loadPercent + "%");

        System.out.printf(Locale.US,
                "Old PID average speed: %.1f ticks/s%n",
                oldPID.averageVelocity);

        System.out.printf(Locale.US,
                "New PID average speed: %.1f ticks/s%n",
                newPID.averageVelocity);

        System.out.printf(Locale.US,
                "Old PID overshoot: %.2f%%%n",
                oldPID.overshoot);

        System.out.printf(Locale.US,
                "New PID overshoot: %.2f%%%n",
                newPID.overshoot);

        System.out.printf(Locale.US,
                "Old PID peak: %.1f ticks/s%n",
                oldPID.peakVelocity);

        System.out.printf(Locale.US,
                "New PID peak: %.1f ticks/s%n",
                newPID.peakVelocity);

        double improvement = oldPID.overshoot - newPID.overshoot;

        System.out.printf(Locale.US,
                "Overshoot reduction: %.2f percentage points%n",
                improvement);
    }

    private static Result simulate(
            int loadPercent,
            boolean internalLimit) throws IOException {

        // Old PID uses 100% internally
        // New PID knows the 40% limit
        VelocityPID pid;

        if (internalLimit) {
            pid = new VelocityPID(0.005, 0.01, 0, powerLimit);
        } else {
            pid = new VelocityPID(0.005, 0.01, 0);
        }

        double velocity = 0;
        double totalVelocity = 0;
        double peakVelocity = 0;

        int samples = 0;

        String version = internalLimit ? "new" : "old";

        String file = "/tmp/ftc-pid-antiwindup-"
                + loadPercent + "-" + version + ".csv";

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println("Time,Target,Velocity,Power,Load");

            for (int i = 0; i < 600; i++) {

                double time = i * deltaTime;

                // Resistance between 4 and 7 seconds
                double load = 0;

                if (time >= 4 && time < 7) {
                    load = loadPercent / 100.0;
                }

                // PID calculation
                double pidPower = pid.calculate(
                        target,
                        velocity,
                        deltaTime
                );

                // Final 40% motor power limit
                double power = Math.max(
                        -powerLimit,
                        Math.min(powerLimit, pidPower)
                );

                // Simulated motor
                double requestedVelocity =
                        power * maxVelocity * (1 - load);

                velocity += (requestedVelocity - velocity)
                        * deltaTime / responseTime;

                // Average speed near end of load
                if (time >= 6 && time < 7) {
                    totalVelocity += velocity;
                    samples++;
                }

                // Maximum speed after removing load
                if (time >= 7 && time < 9) {
                    peakVelocity = Math.max(
                            peakVelocity,
                            velocity
                    );
                }

                writer.printf(Locale.US,
                        "%.2f,%.0f,%.2f,%.3f,%.0f%n",
                        time,
                        target,
                        velocity,
                        power,
                        load * 100
                );
            }
        }

        Result result = new Result();

        result.averageVelocity = totalVelocity / samples;
        result.peakVelocity = peakVelocity;

        result.overshoot = Math.max(
                0,
                (peakVelocity - target) / target * 100
        );

        return result;
    }

    static class Result {

        double averageVelocity;
        double peakVelocity;
        double overshoot;
    }
}
