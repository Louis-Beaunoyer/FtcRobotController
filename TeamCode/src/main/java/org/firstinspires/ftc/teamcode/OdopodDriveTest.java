
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

@TeleOp(name = "TEST - Odopod Drive", group = "Tests")
public class OdopodDriveTest extends OpMode {

    private final DcMotor[] motors = new DcMotor[4];

    // Front left, front right, back left, back right
    private final int[] forwardSigns = {1, 1, -1, -1};

    private static final double TICKS_PER_CM = 198.21;

    // Test distance: 100 cm
    private static final double TEST_DISTANCE_CM = 100.0;

    // Stop motors 2 cm before the target
    private static final double STOP_EARLY_CM = 2.0;

    private static final double MAX_SPEED = 0.22;
    private static final double MIN_SPEED = 0.08;
    private static final double SLOWDOWN_DISTANCE_CM = 12.0;

    private static final double HEADING_KP = 0.008;
    private static final double MAX_CORRECTION = 0.04;

    private DcMotor parallelPod;
    private ImuController imuController;

    private int startTicks;
    private int targetTicks;
    private int stopTicks;
    private int lastProgressTicks;
    private int ticksAtStop;

    private double targetYaw;
    private double yawError;
    private double correction;
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

        motors[0] = hardwareMap.get(
                DcMotor.class,
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        motors[1] = hardwareMap.get(
                DcMotor.class,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        motors[2] = hardwareMap.get(
                DcMotor.class,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

        motors[3] = hardwareMap.get(
                DcMotor.class,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

        // Motor directions confirmed in our previous tests
        motors[0].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[1].setDirection(DcMotorSimple.Direction.FORWARD);
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3].setDirection(DcMotorSimple.Direction.FORWARD);

        for (DcMotor motor : motors) {
            motor.setPower(0);
            motor.setZeroPowerBehavior(
                    DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        // Parallel odopod
        parallelPod = hardwareMap.get(
                DcMotor.class,
                Config.ODOMETRY_PARALLEL_ENCODER_NAME);

        // Louis's IMU controller
        IMU imu = hardwareMap.get(IMU.class, Config.IMU_NAME);
        imuController = new ImuController(imu);

        imu.initialize(imuController.getInitializationParameters());

        startTicks = -parallelPod.getCurrentPosition();

        telemetry.addLine("X = Drive 100 cm");
        telemetry.addLine("Circle = Emergency stop");
        telemetry.addLine("Triangle is disabled");
        telemetry.update();
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

        int currentTicks = -parallelPod.getCurrentPosition();
        int deltaTicks = currentTicks - startTicks;

        if (moving) {

            if (deltaTicks >= stopTicks) {

                stopDrive("Early stop reached");

            } else if (deltaTicks < -100) {

                stopDrive("Wrong odopod direction");

            } else if (timer.seconds() > 8.0) {

                stopDrive("Timeout - stopped");

            } else {

                // Check that the odopod is still moving
                if (deltaTicks >= lastProgressTicks + 40) {
                    lastProgressTicks = deltaTicks;
                    progressTimer.reset();
                }

                if (progressTimer.seconds() > 2.0) {

                    stopDrive("No odopod movement");

                } else {

                    driveForward(deltaTicks);
                }
            }
        }

        // Keep reading the odopod after motor stop
        int finalTicks =
                -parallelPod.getCurrentPosition() - startTicks;

        double elapsedTime = moving
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

        telemetry.addData("Time", "%.2f s", elapsedTime);

        telemetry.update();
    }

    private void startDrive() {

        startTicks = -parallelPod.getCurrentPosition();

        // Full distance target: 100 cm
        targetTicks = (int) Math.round(
                TEST_DISTANCE_CM * TICKS_PER_CM);

        // Stop motors at approximately 98 cm
        stopTicks = (int) Math.round(
                (TEST_DISTANCE_CM - STOP_EARLY_CM)
                        * TICKS_PER_CM);

        targetYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(targetYaw)) {
            stopDrive("IMU error");
            return;
        }

        yawError = 0;
        correction = 0;
        currentSpeed = 0;

        lastProgressTicks = 0;
        ticksAtStop = 0;
        finalTime = 0;

        timer.reset();
        progressTimer.reset();

        moving = true;
        status = "Driving 100 cm";
    }

    private void driveForward(int deltaTicks) {

        double currentYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(currentYaw)) {
            stopDrive("IMU error");
            return;
        }

        yawError = AngleUnit.normalizeDegrees(
                currentYaw - targetYaw);

        if (Math.abs(yawError) > 20.0) {
            stopDrive("Heading error - stopped");
            return;
        }

        double remainingCm =
                (targetTicks - deltaTicks) / TICKS_PER_CM;

        // Slow down gradually over the last 12 cm
        double speedFactor = Range.clip(
                remainingCm / SLOWDOWN_DISTANCE_CM,
                0.0,
                1.0);

        currentSpeed = MIN_SPEED
                + (MAX_SPEED - MIN_SPEED) * speedFactor;

        correction = Range.clip(
                yawError * HEADING_KP,
                -MAX_CORRECTION,
                MAX_CORRECTION);

        double leftPower = Range.clip(
                currentSpeed + correction,
                0.04,
                0.30);

        double rightPower = Range.clip(
                currentSpeed - correction,
                0.04,
                0.30);

        motors[0].setPower(
                leftPower * forwardSigns[0]);

        motors[1].setPower(
                rightPower * forwardSigns[1]);

        motors[2].setPower(
                leftPower * forwardSigns[2]);

        motors[3].setPower(
                rightPower * forwardSigns[3]);
    }

    private void stopDrive(String reason) {

        if (moving) {
            finalTime = timer.seconds();
        }

        ticksAtStop =
                -parallelPod.getCurrentPosition() - startTicks;

        moving = false;
        finished = true;

        currentSpeed = 0;
        correction = 0;

        status = reason;

        for (DcMotor motor : motors) {
            motor.setPower(0);
        }
    }

    @Override
    public void stop() {

        for (DcMotor motor : motors) {
            motor.setPower(0);
        }
    }
}
