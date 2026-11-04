package org.firstinspires.ftc.teamcode;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;


public class Intake {
    private DcMotorEx intakeMotor;
    public Intake(DcMotorEx motor) {
        this.intakeMotor = motor;
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
