
package org.firstinspires.ftc.teamcode;

public class VelocityPID {

    double kP;
    double kI;
    double kD;

    double integral = 0;
    double previousError = 0;

    double maxOutput;

    boolean firstRun = true;

    public VelocityPID(double kP, double kI, double kD) {
        this(kP, kI, kD, 1.0);
    }

    public VelocityPID(double kP, double kI, double kD,
                       double maxOutput) {

        if (!Double.isFinite(maxOutput)
                || maxOutput <= 0
                || maxOutput > 1) {
            throw new IllegalArgumentException(
                    "Output limit must be between 0 and 1"
            );
        }

        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.maxOutput = maxOutput;
    }

    public double calculate(double targetVelocity,
                            double actualVelocity,
                            double deltaTime) {

        if (!Double.isFinite(deltaTime) || deltaTime <= 0) {
            return 0;
        }

        double error = targetVelocity - actualVelocity;

        double derivative = 0;

        if (!firstRun) {
            derivative = (error - previousError) / deltaTime;
        }

        double newIntegral = integral + error * deltaTime;

        double output = kP * error
                + kI * newIntegral
                + kD * derivative;

        // Prevent integral windup during saturation
        boolean upperSaturation =
                output > maxOutput && error > 0;

        boolean lowerSaturation =
                output < -maxOutput && error < 0;

        if (!upperSaturation && !lowerSaturation) {
            integral = newIntegral;
        }

        // Limit the output
        output = Math.max(
                -maxOutput,
                Math.min(maxOutput, output)
        );

        previousError = error;
        firstRun = false;

        return output;
    }

    public void reset() {
        integral = 0;
        previousError = 0;
        firstRun = true;
    }
}
