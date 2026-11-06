package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;


@TeleOp(name = "Main Robot OpMode Dindons", group = "Robot")
public class RobotOPMode extends OpMode{


    Intake intake;

    @Override
    public void init(){
        DcMotorEx intakeMotor = hardwareMap.get(DcMotorEx.class, Config.INTAKE_MOTOR_NAME);
        intake = new Intake(intakeMotor, gamepad1, gamepad2);



    }

    public void loop(){
        intake.RunIntake();

    }


}