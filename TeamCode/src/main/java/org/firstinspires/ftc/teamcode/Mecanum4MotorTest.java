package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "TEST - Mecanum 4 Motors", group = "Tests")
public class Mecanum4MotorTest extends OpMode {

    private DcMotorEx frontLeft;
    private DcMotorEx frontRight;
    private DcMotorEx backLeft;
    private DcMotorEx backRight;

    private IMU imu;

    // Low power for initial physical testing.
    private static final double SPEED_LIMIT = 0.20;
    private static final double DEAD_ZONE = 0.05;

    private boolean motorsEnabled = false;
    private boolean previousY = false;
    private boolean previousA = false;

    private String status = "LOCKED";

    @Override
    public void init() {

        telemetry = new MultipleTelemetry(
                telemetry,
                FtcDashboard.getInstance().getTelemetry()
        );

        // Get the four drive motors.
        frontLeft = hardwareMap.get(
                DcMotorEx.class,
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        frontRight = hardwareMap.get(
                DcMotorEx.class,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        backLeft = hardwareMap.get(
                DcMotorEx.class,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        backRight = hardwareMap.get(
                DcMotorEx.class,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        // Candidate directions based on previous physical tests.
        // Rear directions are opposite to the old Mecanum test.
        frontLeft.setDirection(
                DcMotorSimple.Direction.REVERSE);

        frontRight.setDirection(
                DcMotorSimple.Direction.FORWARD);

        backLeft.setDirection(
                DcMotorSimple.Direction.FORWARD);

        backRight.setDirection(
                DcMotorSimple.Direction.REVERSE);

        // Stop all drive motors before testing.
        stopMotors();

        // Configure motors for open-loop control.
        configureMotor(frontLeft);
        configureMotor(frontRight);
        configureMotor(backLeft);
        configureMotor(backRight);

        // IMU orientation validated with Pedro Pathing.
        imu = hardwareMap.get(IMU.class, Config.IMU_NAME);

        RevHubOrientationOnRobot orientation =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.LEFT
                );

        imu.initialize(new IMU.Parameters(orientation));

        motorsEnabled = false;
        previousY = false;
        previousA = false;
        status = "LOCKED";

        telemetry.addLine("MECANUM TEST - CORRECTED");
        telemetry.addLine("Triangle / Y: Unlock");
        telemetry.addLine("Circle / B: Lock and stop");
        telemetry.addLine("L1: Hold for Robot Relative");
        telemetry.addLine("X / A: Reset IMU yaw when stationary");
        telemetry.addData("Power limit", "%.0f%%",
                SPEED_LIMIT * 100);
        telemetry.addData("Safety", status);
        telemetry.update();
    }

    private void configureMotor(DcMotorEx motor) {

        motor.setPower(0);

        motor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        motor.setMode(
                DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void start() {

        imu.resetYaw();
        stopMotors();
    }

    @Override
    public void loop() {

        // PlayStation left stick:
        // Up = forward, right = strafe right.
        double forward = applyDeadZone(
                -gamepad1.left_stick_y);

        double right = applyDeadZone(
                gamepad1.left_stick_x);

        // PlayStation right stick:
        // Right = clockwise rotation.
        double rotate = applyDeadZone(
                gamepad1.right_stick_x);

        boolean controlsNeutral =
                forward == 0 &&
                right == 0 &&
                rotate == 0;

        // Circle / B: lock motors immediately.
        if (gamepad1.b) {

            motorsEnabled = false;
            status = "LOCKED BY DRIVER";
            stopMotors();
        }

        // Triangle / Y: unlock only with neutral sticks.
        boolean pressedY =
                gamepad1.y && !previousY;

        previousY = gamepad1.y;

        if (pressedY &&
                !gamepad1.b &&
                controlsNeutral) {

            motorsEnabled = true;
            status = "ENABLED";
        }

        // X / A: reset field reference when stationary.
        boolean pressedA =
                gamepad1.a && !previousA;

        previousA = gamepad1.a;

        if (pressedA && controlsNeutral) {

            stopMotors();
            imu.resetYaw();
        }

        // Read and validate IMU.
        double yaw = imu
                .getRobotYawPitchRollAngles()
                .getYaw(AngleUnit.RADIANS);

        if (!Double.isFinite(yaw)) {

            motorsEnabled = false;
            status = "IMU ERROR - LOCKED";
            stopMotors();
        }

        // L1 temporarily selects robot-relative driving.
        boolean robotRelative = gamepad1.left_bumper;

        if (!motorsEnabled) {

            stopMotors();

        } else if (robotRelative) {

            drive(forward, right, rotate);

        } else {

            driveFieldRelative(
                    forward,
                    right,
                    rotate,
                    yaw
            );
        }

        // Safety and drive mode.
        telemetry.addData("Safety", status);

        telemetry.addData(
                "Drive Mode",
                robotRelative
                        ? "ROBOT RELATIVE (L1)"
                        : "FIELD RELATIVE"
        );

        telemetry.addData("Forward", "%.3f", forward);
        telemetry.addData("Strafe", "%.3f", right);
        telemetry.addData("Turn", "%.3f", rotate);

        telemetry.addData(
                "Angle IMU",
                "%.2f deg",
                Math.toDegrees(yaw)
        );

        // Motor power.
        telemetry.addData(
                "Front Left", "%.3f",
                frontLeft.getPower());

        telemetry.addData(
                "Front Right", "%.3f",
                frontRight.getPower());

        telemetry.addData(
                "Back Left", "%.3f",
                backLeft.getPower());

        telemetry.addData(
                "Back Right", "%.3f",
                backRight.getPower());

        // Encoder positions.
        telemetry.addData(
                "Encoder FL",
                frontLeft.getCurrentPosition());

        telemetry.addData(
                "Encoder FR",
                frontRight.getCurrentPosition());

        telemetry.addData(
                "Encoder BL",
                backLeft.getCurrentPosition());

        telemetry.addData(
                "Encoder BR",
                backRight.getCurrentPosition());

        // Motor velocities.
        telemetry.addData(
                "Velocity FL (ticks/s)",
                frontLeft.getVelocity());

        telemetry.addData(
                "Velocity FR (ticks/s)",
                frontRight.getVelocity());

        telemetry.addData(
                "Velocity BL (ticks/s)",
                backLeft.getVelocity());

        telemetry.addData(
                "Velocity BR (ticks/s)",
                backRight.getVelocity());

        telemetry.addData(
                "Speed Limit",
                "%.0f%%",
                SPEED_LIMIT * 100
        );

        telemetry.addData(
                "Dead Zone",
                "%.0f%%",
                DEAD_ZONE * 100
        );

        telemetry.update();
    }

    private double applyDeadZone(double value) {

        if (Math.abs(value) < DEAD_ZONE) {
            return 0;
        }

        return value;
    }

    // Field-relative control using the IMU yaw.
    private void driveFieldRelative(
            double forward,
            double right,
            double rotate,
            double yaw) {

        // Convert desired field movement to robot movement.
        double robotForward =
                forward * Math.cos(yaw)
                - right * Math.sin(yaw);

        double robotRight =
                forward * Math.sin(yaw)
                + right * Math.cos(yaw);

        drive(robotForward, robotRight, rotate);
    }

    // Mecanum drive calculations.
    // Inputs:
    // forward > 0 = forward
    // right > 0 = strafe right
    // rotate > 0 = turn right
    private void drive(
            double forward,
            double right,
            double rotate) {

        double frontLeftPower =
                forward + right + rotate;

        double frontRightPower =
                forward - right - rotate;

        double backLeftPower =
                forward - right + rotate;

        double backRightPower =
                forward + right - rotate;

        // Normalize motor commands every loop.
        double maxPower = Math.max(
                1.0,
                Math.max(
                        Math.max(
                                Math.abs(frontLeftPower),
                                Math.abs(frontRightPower)
                        ),
                        Math.max(
                                Math.abs(backLeftPower),
                                Math.abs(backRightPower)
                        )
                )
        );

        frontLeft.setPower(
                SPEED_LIMIT * frontLeftPower / maxPower);

        frontRight.setPower(
                SPEED_LIMIT * frontRightPower / maxPower);

        backLeft.setPower(
                SPEED_LIMIT * backLeftPower / maxPower);

        backRight.setPower(
                SPEED_LIMIT * backRightPower / maxPower);
    }

    // Explicitly write zero to all four motors.
    private void stopMotors() {

        if (frontLeft != null) {
            frontLeft.setPower(0);
        }

        if (frontRight != null) {
            frontRight.setPower(0);
        }

        if (backLeft != null) {
            backLeft.setPower(0);
        }

        if (backRight != null) {
            backRight.setPower(0);
        }
    }

    @Override
    public void stop() {

        motorsEnabled = false;
        status = "STOPPED";
        stopMotors();
    }
}
