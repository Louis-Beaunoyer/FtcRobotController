
package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "TEST - Mecanum 4 Motors", group = "Tests")
public class Mecanum4MotorTest extends OpMode {

    DcMotorEx frontLeft;
    DcMotorEx frontRight;
    DcMotorEx backLeft;
    DcMotorEx backRight;

    IMU imu;

    double speedLimit = 0.20;
    double deadZone = 0.05;

    boolean motorsEnabled = false;
    boolean previousY = false;

    @Override
    public void init() {

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry()
        );

        // Four drive motors
        frontLeft = hardwareMap.get(
                DcMotorEx.class, Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        frontRight = hardwareMap.get(
                DcMotorEx.class, Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        backLeft = hardwareMap.get(
                DcMotorEx.class, Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        backRight = hardwareMap.get(
                DcMotorEx.class, Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        // Motor directions
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        // Braking
        frontLeft.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        frontRight.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        backLeft.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        backRight.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        // Open-loop control for initial hardware testing
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        drive(0, 0, 0);

        // IMU initialization
        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientation =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                );

        imu.initialize(new IMU.Parameters(orientation));

        motorsEnabled = false;
        previousY = false;

        telemetry.addData("Safety", "LOCKED");
        telemetry.addData("Drive Mode", "Field Relative");
        telemetry.addData("Speed Limit", "20%");
        telemetry.update();
    }

    @Override
    public void start() {

        imu.resetYaw();
    }

    @Override
    public void loop() {

        double forward =
                applyDeadZone(-gamepad1.left_stick_y);

        double right =
                applyDeadZone(gamepad1.left_stick_x);

        double rotate =
                applyDeadZone(gamepad1.right_stick_x);

        boolean controlsNeutral =
                forward == 0 &&
                        right == 0 &&
                        rotate == 0;

        // B: Lock the motors
        if (gamepad1.b) {
            motorsEnabled = false;
        }

        // Y: Unlock only when joysticks are neutral
        if (gamepad1.y &&
                !previousY &&
                !gamepad1.b &&
                controlsNeutral) {

            motorsEnabled = true;
        }

        previousY = gamepad1.y;

        if (!motorsEnabled) {

            drive(0, 0, 0);

        } else {

            driveFieldRelative(forward, right, rotate);
        }

        // Safety telemetry
        telemetry.addData(
                "Safety",
                motorsEnabled ? "ENABLED" : "LOCKED"
        );

        telemetry.addData("Drive Mode", "Field Relative");

        // Joystick telemetry
        telemetry.addData("Forward", forward);
        telemetry.addData("Strafe", right);
        telemetry.addData("Turn", rotate);

        // IMU telemetry
        telemetry.addData(
                "Angle IMU",
                imu.getRobotYawPitchRollAngles()
                        .getYaw(AngleUnit.DEGREES)
        );

        // Motor power telemetry
        telemetry.addData("Front Left", frontLeft.getPower());
        telemetry.addData("Front Right", frontRight.getPower());
        telemetry.addData("Back Left", backLeft.getPower());
        telemetry.addData("Back Right", backRight.getPower());

        // Encoder positions
        telemetry.addData(
                "Encoder FL", frontLeft.getCurrentPosition());

        telemetry.addData(
                "Encoder FR", frontRight.getCurrentPosition());

        telemetry.addData(
                "Encoder BL", backLeft.getCurrentPosition());

        telemetry.addData(
                "Encoder BR", backRight.getCurrentPosition());

        // Motor velocities
        telemetry.addData(
                "Velocity FL (ticks/s)", frontLeft.getVelocity());

        telemetry.addData(
                "Velocity FR (ticks/s)", frontRight.getVelocity());

        telemetry.addData(
                "Velocity BL (ticks/s)", backLeft.getVelocity());

        telemetry.addData(
                "Velocity BR (ticks/s)", backRight.getVelocity());

        telemetry.addData("Speed Limit", speedLimit * 100 + "%");
        telemetry.addData("Dead Zone", deadZone * 100 + "%");

        telemetry.update();
    }

    private double applyDeadZone(double value) {

        if (Math.abs(value) < deadZone) {
            return 0;
        }

        return value;
    }

    // Field Relative driving
    private void driveFieldRelative(
            double forward,
            double right,
            double rotate) {

        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        double yaw = imu.getRobotYawPitchRollAngles()
                .getYaw(AngleUnit.RADIANS);

        theta = AngleUnit.normalizeRadians(theta - yaw);

        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        drive(newForward, newRight, rotate);
    }

    // Mecanum wheel calculations
    public void drive(
            double forward,
            double right,
            double rotate) {

        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backLeftPower = forward - right + rotate;
        double backRightPower = forward + right - rotate;

        double maxPower = 1.0;

        maxPower = Math.max(
                maxPower, Math.abs(frontLeftPower));

        maxPower = Math.max(
                maxPower, Math.abs(frontRightPower));

        maxPower = Math.max(
                maxPower, Math.abs(backLeftPower));

        maxPower = Math.max(
                maxPower, Math.abs(backRightPower));

        frontLeft.setPower(
                speedLimit * frontLeftPower / maxPower);

        frontRight.setPower(
                speedLimit * frontRightPower / maxPower);

        backLeft.setPower(
                speedLimit * backLeftPower / maxPower);

        backRight.setPower(
                speedLimit * backRightPower / maxPower);
    }

    @Override
    public void stop() {

        motorsEnabled = false;

        if (frontLeft != null) frontLeft.setPower(0);
        if (frontRight != null) frontRight.setPower(0);
        if (backLeft != null) backLeft.setPower(0);
        if (backRight != null) backRight.setPower(0);
    }
}
