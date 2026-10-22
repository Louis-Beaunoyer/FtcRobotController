
package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Mecanum TeleOp")
public class MecanumTeleOp extends OpMode {

    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor backLeft;
    DcMotor backRight;
    DcMotor intake;

    IMU imu;

    double speedLimit = 0.4;
    double deadZone = 0.05;

    boolean motorsEnabled = false;
    boolean previousY = false;

    @Override
    public void init() {

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry()
        );

        frontLeft = hardwareMap.get(DcMotor.class, "front_left_drive");
        frontRight = hardwareMap.get(DcMotor.class, "front_right_drive");
        backLeft = hardwareMap.get(DcMotor.class, "back_left_drive");
        backRight = hardwareMap.get(DcMotor.class, "back_right_drive");
        intake = hardwareMap.get(DcMotor.class, "intake");

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        drive(0, 0, 0);
        intake.setPower(0);

        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientationOnRobot =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                );

        imu.initialize(new IMU.Parameters(orientationOnRobot));

        motorsEnabled = false;
        previousY = false;

        telemetry.addData("Safety", "LOCKED - Press Y to enable");
        telemetry.update();
    }

    @Override
    public void start() {

        // Set the field reference when TeleOp starts
        imu.resetYaw();
    }

    @Override
    public void loop() {

        double forward = applyDeadZone(-gamepad1.left_stick_y);
        double right = applyDeadZone(gamepad1.left_stick_x);
        double rotate = applyDeadZone(gamepad1.right_stick_x);

        boolean controlsNeutral =
                forward == 0 &&
                        right == 0 &&
                        rotate == 0 &&
                        gamepad1.right_trigger <= 0.1 &&
                        gamepad1.left_trigger <= 0.1;

        // Lock all motors
        if (gamepad1.b) {
            motorsEnabled = false;
        }

        // Enable motors when controls are neutral
        if (gamepad1.y && !previousY &&
                !gamepad1.b && controlsNeutral) {
            motorsEnabled = true;
        }

        previousY = gamepad1.y;

        if (!motorsEnabled) {

            drive(0, 0, 0);
            intake.setPower(0);

        } else {

            // Always use Field Relative driving
            driveFieldRelative(forward, right, rotate);

            // Intake control
            if (gamepad1.right_trigger > 0.1 &&
                    gamepad1.left_trigger <= 0.1) {

                intake.setPower(0.4);

            } else if (gamepad1.left_trigger > 0.1 &&
                    gamepad1.right_trigger <= 0.1) {

                intake.setPower(-0.4);

            } else {

                intake.setPower(0);
            }
        }

        // Telemetry
        telemetry.addData("Safety",
                motorsEnabled ? "ENABLED" : "LOCKED");

        telemetry.addData("Drive Mode", "Field Relative");

        telemetry.addData("Forward", forward);
        telemetry.addData("Strafe", right);
        telemetry.addData("Turn", rotate);

        telemetry.addData("Angle IMU",
                imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));

        telemetry.addData("Front Left", frontLeft.getPower());
        telemetry.addData("Front Right", frontRight.getPower());
        telemetry.addData("Back Left", backLeft.getPower());
        telemetry.addData("Back Right", backRight.getPower());

        telemetry.addData("Encoder FL", frontLeft.getCurrentPosition());
        telemetry.addData("Encoder FR", frontRight.getCurrentPosition());
        telemetry.addData("Encoder BL", backLeft.getCurrentPosition());
        telemetry.addData("Encoder BR", backRight.getCurrentPosition());

        telemetry.addData("Intake Power", intake.getPower());
        telemetry.addData("Intake Encoder", intake.getCurrentPosition());

        telemetry.addData("Speed Limit", speedLimit * 100 + "%");
        telemetry.addData("Dead Zone", deadZone * 100 + "%");

        telemetry.update();
    }

    @Override
    public void stop() {

        motorsEnabled = false;

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        intake.setPower(0);
    }

    private double applyDeadZone(double value) {

        if (Math.abs(value) < deadZone) {
            return 0;
        }

        return value;
    }

    // Field Relative movement
    private void driveFieldRelative(
            double forward,
            double right,
            double rotate) {

        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        theta = AngleUnit.normalizeRadians(
                theta -
                        imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS)
        );

        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        drive(newForward, newRight, rotate);
    }

    // Robot Relative motor calculations
    public void drive(
            double forward,
            double right,
            double rotate) {

        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;

        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));

        frontLeft.setPower(speedLimit * frontLeftPower / maxPower);
        frontRight.setPower(speedLimit * frontRightPower / maxPower);
        backLeft.setPower(speedLimit * backLeftPower / maxPower);
        backRight.setPower(speedLimit * backRightPower / maxPower);
    }
}
