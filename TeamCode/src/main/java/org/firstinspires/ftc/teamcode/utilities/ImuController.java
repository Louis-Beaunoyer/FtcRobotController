package org.firstinspires.ftc.teamcode.utilities;


import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class ImuController {
    public ImuController(IMU imu){
        this.imu = imu;
    }
    IMU imu;


    public IMU.Parameters getInitializationParameters(){
        return new IMU.Parameters(new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.UP, RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));
    }
    public double getYawInDegrees(){
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }
    public double getYawInRadians(){
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }
    public double getPitchInDegrees(){
        return imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.DEGREES);
    }
    public double getPitchInRadians(){
        return imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.RADIANS);
    }
    public double getRollInDegrees(){
        return imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES);
    }
    public double getRollInRadians(){
        return imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.RADIANS);
    }
    public void resetYaw(){
        imu.resetYaw();
    }
    public void getAngularVelocityInDegrees(){
        imu.getRobotAngularVelocity(AngleUnit.DEGREES);
    }
    public void getAngularVelocityInRadians(){
        imu.getRobotAngularVelocity(AngleUnit.RADIANS);
    }
}
