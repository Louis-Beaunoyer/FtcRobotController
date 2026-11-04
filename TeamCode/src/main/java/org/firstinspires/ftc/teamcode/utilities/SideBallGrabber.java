package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.Config;

public class SideBallGrabber {

    SideBallGrabber(DcMotorEx leftSideMotor, DcMotorEx rightSideMotor){
        this.leftMotor = leftSideMotor;
        this.leftMotor = rightSideMotor;
        this.defaultGrabberSpeed = Config.DEFAULT_BALL_GRABBER_SPEED;

    }

    DcMotorEx leftMotor;
    DcMotorEx rightMotor;
    private double defaultGrabberSpeed;
    private double grabberSpeed;

    public void setGrabberSpeed(double speed){
        grabberSpeed = speed;
    }
    public double getGrabberSpeed(){
        return grabberSpeed;
    }
    public void toogleGrabber(Boolean useDefaultSpeed) {
        if (leftMotor.getPower() == 0) {
            if (useDefaultSpeed) {
                setGrabberSpeed(defaultGrabberSpeed);
            } else {
                setGrabberSpeed(grabberSpeed);
            }


        }else{
            setGrabberSpeed(0);
        }
    }


}
