package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.teamcode.utilities.SideBallGrabber;


@TeleOp(name = "Main Robot OpMode Dindons", group = "Robot")
public class RobotOPMode extends OpMode{


    Intake intake;
    SideBallGrabber sideBallGrabber;

    @Override
    public void init(){
        DcMotorEx intakeMotor = hardwareMap.get(DcMotorEx.class, Config.INTAKE_MOTOR_NAME);
        CRServo leftSideBallGrabberServo = hardwareMap.get(CRServo.class, Config.SIDE_BALL_GRABBER_LEFT_MOTOR_NAME);
        CRServo rightSideBallGrabberServo = hardwareMap.get(CRServo.class, Config.SIDE_BALL_GRABBER_RIGHT_MOTOR_NAME);
        intake = new Intake(intakeMotor, gamepad1, gamepad2);
        sideBallGrabber = new SideBallGrabber(leftSideBallGrabberServo, rightSideBallGrabberServo);




    }

    public void loop(){
        intake.RunIntake();
        sideBallGrabber.setGrabberSpeed(gamepad1.left_trigger);
        if (gamepad1.a){
            sideBallGrabber.toogleGrabber(false);
        }

    }


}