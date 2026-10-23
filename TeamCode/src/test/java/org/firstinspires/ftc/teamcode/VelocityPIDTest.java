
package org.firstinspires.ftc.teamcode;

public class VelocityPIDTest {

    public static void main(String[] args) {

        VelocityPID pid = new VelocityPID(0.1, 0, 0);

        // Motor too slow
        check("Positive correction",
                pid.calculate(5, 2, 0.1), 0.3);

        pid.reset();

        // Motor too fast
        check("Negative correction",
                pid.calculate(2, 5, 0.1), -0.3);

        pid.reset();

        // Maximum output
        check("Power limit",
                pid.calculate(20, 0, 0.1), 1.0);

        pid.reset();

        // Invalid time interval
        check("Zero delta time",
                pid.calculate(5, 0, 0), 0.0);

        System.out.println("ALL PID TESTS PASSED");
    }

    private static void check(String name,
                              double actual,
                              double expected) {

        if (Math.abs(actual - expected) > 0.000001) {
            throw new AssertionError(name + " FAILED");
        }

        System.out.println(name + " PASSED");
    }
}
