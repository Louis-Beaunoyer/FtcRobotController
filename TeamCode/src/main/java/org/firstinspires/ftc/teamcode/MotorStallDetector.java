
package org.firstinspires.ftc.teamcode;

public class MotorStallDetector {

    double minimumPower;
    double minimumVelocity;
    double stallTimeout;

    double stallTimer = 0;

    boolean stalled = false;
    boolean sensorFault = false;

    public MotorStallDetector(
            double minimumPower,
            double minimumVelocity,
            double stallTimeout) {

        if (!Double.isFinite(minimumPower)
                || !Double.isFinite(minimumVelocity)
                || !Double.isFinite(stallTimeout)
                || minimumPower <= 0
                || minimumPower > 1
                || minimumVelocity < 0
                || stallTimeout <= 0) {

            throw new IllegalArgumentException(
                    "Invalid stall detector parameters"
            );
        }

        this.minimumPower = minimumPower;
        this.minimumVelocity = minimumVelocity;
        this.stallTimeout = stallTimeout;
    }

    public boolean update(
            double motorPower,
            double motorVelocity,
            double deltaTime) {

        // Keep motor locked after a fault
        if (stalled) {
            return true;
        }

        // Invalid measurements or timing
        if (!Double.isFinite(motorPower)
                || !Double.isFinite(motorVelocity)
                || !Double.isFinite(deltaTime)
                || deltaTime <= 0) {

            sensorFault = true;
            stalled = true;
            return true;
        }

        boolean motorTryingToMove =
                Math.abs(motorPower) >= minimumPower;

        boolean motorNotMoving =
                Math.abs(motorVelocity) <= minimumVelocity;

        if (motorTryingToMove && motorNotMoving) {

            stallTimer += deltaTime;

            if (stallTimer >= stallTimeout) {
                stalled = true;
            }

        } else {
            stallTimer = 0;
        }

        return stalled;
    }

    public boolean isStalled() {
        return stalled;
    }

    public boolean hasSensorFault() {
        return sensorFault;
    }

    public double getStallTimer() {
        return stallTimer;
    }

    // Reset only after checking the motor and sensors
    public void reset() {
        stalled = false;
        sensorFault = false;
        stallTimer = 0;
    }
}
