package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Config;

public class DriveTrain {
    public DriveTrain(DcMotorEx frontLeft, DcMotorEx frontRight, DcMotorEx backLeft, DcMotorEx backRight, ImuController imu, Config config){
        this.frontLeftMotor = frontLeft;
        this.frontRightMotor = frontRight;
        this.backLeftMotor = backLeft;
        this.backRightMotor = backRight;
        this.imu = imu;
        this.config = config;
    }
    DcMotorEx frontLeftMotor;
    DcMotorEx frontRightMotor;
    DcMotorEx backLeftMotor;
    DcMotorEx backRightMotor;
    Config config;
    ImuController imu;
    double maxPower = config.DRIVE_TRAIN_MAX_POWER;
    double maxSpeed = config.DRIVE_TRAIN_MAX_SPEED;


    public void drive(double forward, double right, double rotate){
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backLeftPower = forward - right + rotate;
        double backRightPower = forward + right - rotate;

        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        frontLeftMotor.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightMotor.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftMotor.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightMotor.setPower(maxSpeed * (backRightPower / maxPower));
    }
    public void driveFieldRelative(double forward, double right, double rotate){
        double[] polar = cartesianToPolar(forward, right);
        double theta = rotateAngleByCurrentYaw(polar[1]);
        double[] cartesian = polarToCartesian(polar[0], theta);
        drive(cartesian[0] , cartesian[1], rotate);
    }

    private double[] polarToCartesian(double r, double theta){
        double forward = r * Math.sin(theta);
        double right = r * Math.cos(theta);
        return new double[]{forward, right};
    }
    private double rotateAngleByCurrentYaw(double theta){
        return AngleUnit.normalizeRadians(theta - imu.getYawInRadians());
    }
    private double[] cartesianToPolar(double forward, double right){
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);
        return new double[]{r, theta};

    }

    public enum selectDriveTrainMotor {
        frontLeft,
        frontRight,
        backLeft,
        backRight
    }
    public void setDriveTrainMotor(selectDriveTrainMotor motor, double power){
        switch (motor){
            case frontLeft:
                frontLeftMotor.setPower(power);
                break;
            case frontRight:
                frontRightMotor.setPower(power);
                break;
            case backLeft:
                backLeftMotor.setPower(power);
                break;
            case backRight:
                backRightMotor.setPower(power);
                break;
        }
    }


}


