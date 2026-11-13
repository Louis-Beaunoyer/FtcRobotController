
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.utilities.ImuController;

@TeleOp(name = "TEST - Rotation IMU", group = "Tests")
public class RotationTest extends OpMode {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private ImuController imuController;

    private static final double TEST_ANGLE = 90.0;

    private static final double MAX_SPEED = 0.20;
    private static final double MIN_SPEED = 0.10;
    private static final double SLOWDOWN_ANGLE = 30.0;

    private static final double ANGLE_TOLERANCE = 1.5;
    private static final double MAX_TIME = 12.0;

    private double startYaw;
    private double targetYaw;
    private double currentYaw;
    private double yawError;
    private double motorPower;
    private double finalTime;

    private boolean moving = false;
    private boolean finished = false;
    private boolean previousY = false;

    private String status = "Ready";

    private final ElapsedTime timer = new ElapsedTime();

    @Override
    public void init() {

        frontLeft = hardwareMap.get(
                DcMotor.class,
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        frontRight = hardwareMap.get(
                DcMotor.class,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        backLeft = hardwareMap.get(
                DcMotor.class,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        backRight = hardwareMap.get(
                DcMotor.class,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        // Motor directions confirmed in previous tests
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        setupMotor(frontLeft);
        setupMotor(frontRight);
        setupMotor(backLeft);
        setupMotor(backRight);

        // Use Louis's IMU controller
        IMU imu = hardwareMap.get(IMU.class, Config.IMU_NAME);

        imuController = new ImuController(imu);
        imu.initialize(imuController.getInitializationParameters());

        startYaw = imuController.getYawInDegrees();
        currentYaw = startYaw;
        targetYaw = startYaw;

        telemetry.addLine("Triangle = Turn LEFT 90 degrees");
        telemetry.addLine("Circle = Emergency stop");
        telemetry.addLine("X is disabled");
        telemetry.update();
    }

    private void setupMotor(DcMotor motor) {

        motor.setPower(0);

        motor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void loop() {

        boolean pressedY = gamepad1.y && !previousY;
        previousY = gamepad1.y;

        currentYaw = imuController.getYawInDegrees();

        // Emergency stop
        if (gamepad1.b && !finished) {
            stopMotors("Stopped by driver");
        }

        // Start only one rotation per run
        if (pressedY && !moving && !finished && !gamepad1.b) {
            startRotation();
        }

        if (moving) {
            updateRotation();
        }

        double turned = AngleUnit.normalizeDegrees(
                currentYaw - startYaw);

        if (Double.isFinite(currentYaw)) {
            yawError = AngleUnit.normalizeDegrees(
                    targetYaw - currentYaw);
        }

        telemetry.addData("Status", status);

        telemetry.addData("Start yaw", "%.2f", startYaw);
        telemetry.addData("Target yaw", "%.2f", targetYaw);
        telemetry.addData("Current yaw", "%.2f", currentYaw);

        // Rotation relative to the starting angle
        telemetry.addData("Turned", "%.2f degrees", turned);
        telemetry.addData("Yaw error", "%.2f", yawError);

        telemetry.addData("Motor power", "%.3f", motorPower);

        telemetry.addData("Time", "%.2f s",
                moving ? timer.seconds() : finalTime);

        telemetry.update();
    }

    private void startRotation() {

        startYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(startYaw)) {
            stopMotors("IMU error");
            return;
        }

        // Calculate target even if yaw crosses 180 degrees
        targetYaw = AngleUnit.normalizeDegrees(
                startYaw + TEST_ANGLE);

        currentYaw = startYaw;
        yawError = TEST_ANGLE;
        motorPower = 0;
        finalTime = 0;

        timer.reset();

        moving = true;
        status = "Turning LEFT 90 degrees";
    }

    private void updateRotation() {

        if (!Double.isFinite(currentYaw)) {
            stopMotors("IMU error");
            return;
        }

        double turned = AngleUnit.normalizeDegrees(
                currentYaw - startYaw);

        yawError = AngleUnit.normalizeDegrees(
                targetYaw - currentYaw);

        // Stop if robot rotates in the wrong direction
        if (turned < -4.0) {
            stopMotors("Wrong rotation direction");
            return;
        }

        // Stop near the target angle
        if (Math.abs(yawError) <= ANGLE_TOLERANCE) {
            stopMotors("Target reached");
            return;
        }

        // Stop if the robot passes the target
        if (yawError < -ANGLE_TOLERANCE) {
            stopMotors("Overshoot - stopped");
            return;
        }

        // Safety timeout
        if (timer.seconds() >= MAX_TIME) {
            stopMotors("Timeout - stopped");
            return;
        }

        // Check that the robot actually starts rotating
        if (timer.seconds() > 2.0 && turned < 1.0) {
            stopMotors("No rotation detected");
            return;
        }

        // Slow down during the last 30 degrees
        double speedFactor = Range.clip(
                yawError / SLOWDOWN_ANGLE,
                0.0,
                1.0);

        motorPower = MIN_SPEED
                + (MAX_SPEED - MIN_SPEED) * speedFactor;

        turnLeft(motorPower);
    }

    private void turnLeft(double power) {

        // Motor signs confirmed during our 15-degree tests
        frontLeft.setPower(-power);
        frontRight.setPower(power);

        backLeft.setPower(power);
        backRight.setPower(-power);
    }

    private void stopMotors(String reason) {

        if (moving) {
            finalTime = timer.seconds();
        }

        moving = false;
        finished = true;
        motorPower = 0;
        status = reason;

        frontLeft.setPower(0);
        frontRight.setPower(0);

        backLeft.setPower(0);
        backRight.setPower(0);
    }

    @Override
    public void stop() {

        frontLeft.setPower(0);
        frontRight.setPower(0);

        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
