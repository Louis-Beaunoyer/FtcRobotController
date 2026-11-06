package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Config;

public class SideBallGrabber {

    public SideBallGrabber(Servo leftSideServo, Servo rightSideServo){
        this.leftServo = leftSideServo;
        this.rightServo = rightSideServo;
        this.defaultGrabberSpeed = Config.DEFAULT_BALL_GRABBER_SPEED;

    }

    Servo leftServo;
    Servo rightServo;
    private double defaultGrabberSpeed;
    private double grabberSpeed;

    public void setGrabberSpeed(double speed){
        grabberSpeed = speed;
    }
    public double getGrabberSpeed(){
        return grabberSpeed;
    }
    public void toogleGrabber(Boolean useDefaultSpeed) {
        if (leftServo.getPosition() == 0) {
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
