package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.teamcode.Config;

public class SideBallGrabber {

    public SideBallGrabber(CRServo leftSideServo, CRServo rightSideServo){
        this.leftServo = leftSideServo;
        this.rightServo = rightSideServo;
        this.defaultGrabberSpeed = Config.DEFAULT_BALL_GRABBER_SPEED;

    }

    CRServo leftServo;
    CRServo rightServo;
    private double defaultGrabberSpeed;
    private double grabberSpeed;

    public void setGrabberSpeed(double speed){
        grabberSpeed = speed;
    }
    public double getGrabberSpeed(){
        return grabberSpeed;
    }
    public void toogleGrabber(Boolean useDefaultSpeed) {
        if (leftServo.getPower() == 0) {
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
