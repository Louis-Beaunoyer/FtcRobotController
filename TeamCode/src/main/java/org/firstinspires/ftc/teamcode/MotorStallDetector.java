
package org.firstinspires.ftc.teamcode;

public class MotorStallDetector {

    double minimumPower;
    double minimumVelocity;
    double stallTimeout;

    double stallTimer = 0;
    boolean stalled = false;

    public MotorStallDetector(
            double minimumPower,
            double minimumVelocity,
            double stallTimeout) {

        if (minimumPower <= 0 || minimumPower > 1 ||
                minimumVelocity < 0 || stallTimeout <= 0 ||
                !Double.isFinite(minimumPower) ||
                !Double.isFinite(minimumVelocity) ||
                !Double.isFinite(stallTimeout)) {

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

        // Once blocked, keep the motor locked
        if (stalled) {
            return true;
        }

        if (!Double.isFinite(motorPower) ||
                !Double.isFinite(motorVelocity) ||
                !Double.isFinite(deltaTime) ||
                deltaTime <= 0) {

            stallTimer = 0;
            return false;
        }

        // Check if motor is commanded but not moving
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

    public double getStallTimer() {
        return stallTimer;
    }

    // Only call after checking that the motor is safe
    public void reset() {
        stalled = false;
        stallTimer = 0;
    }
}
