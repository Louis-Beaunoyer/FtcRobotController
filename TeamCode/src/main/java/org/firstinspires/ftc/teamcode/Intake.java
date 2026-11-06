package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.DcMotorEx;

public class Intake {
    private DcMotorEx intakeMotor;
    private Gamepad gamepad1;
    private Gamepad gamepad2;
    public Intake(DcMotorEx motor, Gamepad gamepad1, Gamepad gamepad2) {
        this.intakeMotor = motor;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
    }

    public void RunIntake() {
        if (gamepad1.right_trigger > 0.1) {
            intakeMotor.setPower(gamepad1.right_trigger);
        }
        else {
            intakeMotor.setPower(0);
        }
    }
}
