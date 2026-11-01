package org.firstinspires.ftc.teamcode.utilities;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Config;

public class DriveTrain {
    public DriveTrain(DcMotorEx frontLeft, DcMotorEx frontRight, DcMotorEx backLeft, DcMotorEx backRight, IMU imu){
        this.frontLeftMotor = frontLeft;
        this.frontRightMotor = frontRight;
        this.backLeftMotor = backLeft;
        this.backRightMotor = backRight;
        this.imu = imu;
    }
    DcMotorEx frontLeftMotor;
    DcMotorEx frontRightMotor;
    DcMotorEx backLeftMotor;
    DcMotorEx backRightMotor;
    IMU imu;
    double maxPower = Config.DRIVE_TRAIN_MAX_POWER;
    double maxSpeed = Config.DRIVE_TRAIN_MAX_SPEED;

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
        double r = polar[0];
        double theta = polar[1];
        theta = rotateAngleByCurrentYaw(theta);
        double[] cartesian = polarToCartesian(r, theta);
        drive(cartesian[0], cartesian[1], rotate);
    }

    private double[] polarToCartesian(double r, double theta){
        double forward = r * Math.sin(theta);
        double right = r * Math.cos(theta);
        return new double[]{forward, right};
    }
    private double rotateAngleByCurrentYaw(double theta){
        double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        return AngleUnit.normalizeRadians(theta - currentYaw);
    }
    private double[] cartesianToPolar(double forward, double right){
        double r = Math.hypot(forward, right);
        double theta = Math.atan2(forward, right);
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


