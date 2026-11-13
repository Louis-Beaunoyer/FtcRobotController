package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Config;

public final class PedroMotorSafety {

    private final DcMotorEx[] motors = new DcMotorEx[4];

    private static final String[] MOTOR_NAMES = {
            "Front Left",
            "Front Right",
            "Back Left",
            "Back Right"
    };

    private boolean faultLatched = false;
    private String faultReason = "";

    // Retrieve the same four motors used by the drivetrain.
    // This constructor does not move the motors.
    public PedroMotorSafety(HardwareMap hardwareMap) {

        if (hardwareMap == null) {
            throw new IllegalArgumentException(
                    "HardwareMap cannot be null");
        }

        motors[0] = hardwareMap.get(
                DcMotorEx.class,
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        motors[1] = hardwareMap.get(
                DcMotorEx.class,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        motors[2] = hardwareMap.get(
                DcMotorEx.class,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        motors[3] = hardwareMap.get(
                DcMotorEx.class,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        // Reject duplicate motor mappings.
        for (int i = 0; i < motors.length; i++) {
            for (int j = i + 1; j < motors.length; j++) {

                if (motors[i] == motors[j]) {
                    throw new IllegalStateException(
                            "Duplicate drivetrain motor mapping");
                }
            }
        }
    }

    // Directly request zero power on every motor.
    // Even if one motor reports an error, try the others.
    public boolean stopAll() {

        boolean success = true;
        StringBuilder errors = new StringBuilder();

        for (int i = 0; i < motors.length; i++) {

            try {
                motors[i].setPower(0.0);

            } catch (RuntimeException e) {

                success = false;

                if (errors.length() > 0) {
                    errors.append("; ");
                }

                errors.append(MOTOR_NAMES[i]);
                errors.append(": ");
                errors.append(e.getMessage());
            }
        }

        if (!success) {
            faultLatched = true;
            faultReason = "Motor stop failed: " + errors;
        }

        return success;
    }

    // Record a fault and request an immediate motor stop.
    // The future central OpMode must block new motor commands
    // whenever a fault is latched.
    public boolean emergencyStop(String reason) {

        faultLatched = true;

        faultReason = (reason == null || reason.isEmpty())
                ? "Emergency stop"
                : reason;

        return stopAll();
    }

    public boolean isFaultLatched() {
        return faultLatched;
    }

    public String getFaultReason() {
        return faultReason;
    }
}
