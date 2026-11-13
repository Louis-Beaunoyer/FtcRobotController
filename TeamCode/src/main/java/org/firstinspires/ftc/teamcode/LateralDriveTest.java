
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

@TeleOp(name = "TEST - Lateral Drive", group = "Tests")
public class LateralDriveTest extends OpMode {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private DcMotor lateralPod;
    private ImuController imuController;

    // Calibration from our manual tests
    private static final double TICKS_PER_CM = 198.07;

    // New test distance: 100 cm
    private static final double TEST_DISTANCE_CM = 100.0;

    // Stop motors 1 cm before the target
    private static final double STOP_EARLY_CM = 1.0;

    private static final double MAX_SPEED = 0.18;
    private static final double MIN_SPEED = 0.10;
    private static final double SLOWDOWN_CM = 8.0;

    private static final double HEADING_KP = 0.008;
    private static final double MAX_CORRECTION = 0.04;

    private int startTicks;
    private int targetTicks;
    private int stopTicks;
    private int ticksAtStop;
    private int lastProgressTicks;

    private double targetYaw;
    private double yawError;
    private double currentSpeed;
    private double finalTime;

    private boolean moving = false;
    private boolean finished = false;
    private boolean previousA = false;

    private String status = "Ready";

    private final ElapsedTime timer = new ElapsedTime();
    private final ElapsedTime progressTimer = new ElapsedTime();

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

        // Lateral odopod
        lateralPod = hardwareMap.get(
                DcMotor.class,
                Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME);

        // Use Louis's IMU controller
        IMU imu = hardwareMap.get(IMU.class, Config.IMU_NAME);

        imuController = new ImuController(imu);
        imu.initialize(imuController.getInitializationParameters());

        startTicks = readLateralTicks();

        telemetry.addLine("X = Move 100 cm RIGHT");
        telemetry.addLine("Circle = Emergency stop");
        telemetry.addLine("Triangle is disabled");
        telemetry.update();
    }

    private void setupMotor(DcMotor motor) {

        motor.setPower(0);
        motor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    // Moving right decreases the displayed encoder ticks
    private int readLateralTicks() {
        return -lateralPod.getCurrentPosition();
    }

    private int getRightTicks() {
        return startTicks - readLateralTicks();
    }

    @Override
    public void loop() {

        boolean pressedA = gamepad1.a && !previousA;
        previousA = gamepad1.a;

        // Emergency stop
        if (gamepad1.b && !finished) {
            stopDrive("Stopped by driver");
        }

        // Start only one movement per run
        if (pressedA && !moving && !finished && !gamepad1.b) {
            startDrive();
        }

        int rightTicks = getRightTicks();

        if (moving) {

            if (rightTicks >= stopTicks) {

                stopDrive("Early stop reached");

            } else if (rightTicks < -100) {

                stopDrive("Wrong odopod direction");

            } else if (timer.seconds() > 8.0) {

                stopDrive("Timeout - stopped");

            } else {

                // Check that the lateral odopod is moving
                if (rightTicks >= lastProgressTicks + 40) {
                    lastProgressTicks = rightTicks;
                    progressTimer.reset();
                }

                if (progressTimer.seconds() > 1.5) {

                    stopDrive("No lateral movement");

                } else {

                    driveRight(rightTicks);
                }
            }
        }

        // Continue reading ticks after motor stop
        int finalTicks = getRightTicks();

        double displayedTime = moving
                ? timer.seconds()
                : finalTime;

        telemetry.addData("Status", status);

        telemetry.addData("Target distance", "%.1f cm",
                TEST_DISTANCE_CM);

        telemetry.addData("Target ticks", targetTicks);
        telemetry.addData("Early stop ticks", stopTicks);

        telemetry.addData("Ticks at motor stop", ticksAtStop);
        telemetry.addData("Current ticks", finalTicks);

        telemetry.addData("Measured distance", "%.2f cm",
                finalTicks / TICKS_PER_CM);

        telemetry.addData("Speed", "%.3f", currentSpeed);

        telemetry.addData("Yaw target", "%.2f", targetYaw);
        telemetry.addData("Yaw current", "%.2f",
                imuController.getYawInDegrees());
        telemetry.addData("Yaw error", "%.2f", yawError);

        telemetry.addData("Time", "%.2f s", displayedTime);

        telemetry.update();
    }

    private void startDrive() {

        startTicks = readLateralTicks();

        // Full distance target: 100 cm
        targetTicks = (int) Math.round(
                TEST_DISTANCE_CM * TICKS_PER_CM);

        // Stop motors at approximately 99 cm
        stopTicks = (int) Math.round(
                (TEST_DISTANCE_CM - STOP_EARLY_CM)
                        * TICKS_PER_CM);

        targetYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(targetYaw)) {
            stopDrive("IMU error");
            return;
        }

        yawError = 0;
        currentSpeed = 0;

        ticksAtStop = 0;
        lastProgressTicks = 0;
        finalTime = 0;

        timer.reset();
        progressTimer.reset();

        moving = true;
        status = "Moving RIGHT 100 cm";
    }

    private void driveRight(int rightTicks) {

        double currentYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(currentYaw)) {
            stopDrive("IMU error");
            return;
        }

        yawError = AngleUnit.normalizeDegrees(
                currentYaw - targetYaw);

        if (Math.abs(yawError) > 15.0) {
            stopDrive("Heading error - stopped");
            return;
        }

        double remainingCm =
                (targetTicks - rightTicks) / TICKS_PER_CM;

        // Slow down over the last 8 cm
        double speedFactor = Range.clip(
                remainingCm / SLOWDOWN_CM,
                0.0,
                1.0);

        currentSpeed = MIN_SPEED
                + (MAX_SPEED - MIN_SPEED) * speedFactor;

        // Correct heading using the IMU
        double correction = Range.clip(
                yawError * HEADING_KP,
                -MAX_CORRECTION,
                MAX_CORRECTION);

        // Mecanum strafe right
        frontLeft.setPower(Range.clip(
                currentSpeed + correction, -0.25, 0.25));

        frontRight.setPower(Range.clip(
                -currentSpeed - correction, -0.25, 0.25));

        backLeft.setPower(Range.clip(
                currentSpeed - correction, -0.25, 0.25));

        backRight.setPower(Range.clip(
                -currentSpeed + correction, -0.25, 0.25));
    }

    private void stopDrive(String reason) {

        if (moving) {
            finalTime = timer.seconds();
        }

        ticksAtStop = getRightTicks();

        moving = false;
        finished = true;
        currentSpeed = 0;

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
