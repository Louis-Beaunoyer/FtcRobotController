
package org.firstinspires.ftc.teamcode;

public class VelocityPID {

    double kP;
    double kI;
    double kD;

    double integral = 0;
    double previousError = 0;

    boolean firstRun = true;

    public VelocityPID(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public double calculate(double targetVelocity,
                            double actualVelocity,
                            double deltaTime) {

        if (deltaTime <= 0) {
            return 0;
        }

        double error = targetVelocity - actualVelocity;

        double derivative = 0;

        if (!firstRun) {
            derivative = (error - previousError) / deltaTime;
        }

        // Calculate the correction
        double newIntegral = integral + error * deltaTime;

        double output = kP * error
                + kI * newIntegral
                + kD * derivative;

        // Limit output to motor power range
        output = Math.max(-1.0, Math.min(1.0, output));

        // Prevent integral from growing when output is saturated
        if (Math.abs(output) < 1.0 || kI == 0) {
            integral = newIntegral;
        }

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
