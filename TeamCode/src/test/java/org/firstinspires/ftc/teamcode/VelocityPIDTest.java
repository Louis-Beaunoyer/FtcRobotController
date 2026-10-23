
package org.firstinspires.ftc.teamcode;

public class VelocityPIDTest {

    public static void main(String[] args) {

        // Proportional control
        VelocityPID pid = new VelocityPID(0.1, 0, 0);

        check("P positive", pid.calculate(5, 2, 0.1), 0.3);

        pid.reset();
        check("P negative", pid.calculate(2, 5, 0.1), -0.3);

        // Integral control
        pid = new VelocityPID(0, 0.2, 0);

        check("I first step", pid.calculate(1, 0, 1), 0.2);
        check("I second step", pid.calculate(1, 0, 1), 0.4);

        pid.reset();
        check("I reset", pid.calculate(1, 0, 1), 0.2);

        // Derivative control
        pid = new VelocityPID(0, 0, 0.1);

        check("D first step", pid.calculate(2, 1, 1), 0);
        check("D second step", pid.calculate(3, 1, 1), 0.1);

        pid.reset();
        check("D reset", pid.calculate(3, 1, 1), 0);

        // Power limits
        pid = new VelocityPID(1, 0, 0);

        check("Maximum power", pid.calculate(5, 0, 1), 1);
        check("Minimum power", pid.calculate(-5, 0, 1), -1);

        // Original anti-windup tests
        pid = new VelocityPID(0, 0.5, 0);

        check("Saturation", pid.calculate(4, 0, 1), 1);
        check("Anti windup", pid.calculate(1, 0, 1), 0.5);

        // Invalid time intervals
        pid.reset();

        check("Zero time", pid.calculate(5, 0, 0), 0);
        check("Negative time", pid.calculate(5, 0, -1), 0);

        // New tests: 40% motor power limit
        pid = new VelocityPID(1, 0, 0, 0.4);

        check("40% positive limit",
                pid.calculate(5, 0, 1), 0.4);

        pid.reset();

        check("40% negative limit",
                pid.calculate(-5, 0, 1), -0.4);

        // New anti-windup test at 40%
        pid = new VelocityPID(0, 0.5, 0, 0.4);

        check("40% saturation",
                pid.calculate(4, 0, 1), 0.4);

        check("40% anti windup recovery",
                pid.calculate(0.5, 0, 1), 0.25);

        System.out.println("ALL 18 PID TESTS PASSED");
    }

    private static void check(String name,
                              double actual,
                              double expected) {

        if (Math.abs(actual - expected) > 0.000001) {
            throw new AssertionError(
                    name + " FAILED: expected " + expected +
                            ", got " + actual
            );
        }

        System.out.println(name + " PASSED");
    }
}
