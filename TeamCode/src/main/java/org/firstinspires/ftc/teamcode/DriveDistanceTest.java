
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

@TeleOp(name = "TEST - Drive Distance", group = "Tests")
public class DriveDistanceTest extends OpMode {

    private final DcMotor[] motors = new DcMotor[4];

    // FL, FR, BL, BR
    private final int[] ticksPerTurn = {548, 554, 540, 524};

    // Encoder directions needed to move the robot forward
    private final int[] forwardSigns = {1, 1, -1, -1};

    private static final double WHEEL_DIAMETER_CM = 10.0;
    private static final double SPEED = 0.22;

    private static final double HEADING_KP = 0.008;
    private static final double MAX_CORRECTION = 0.10;

    private DcMotor parallelPod;
    private DcMotor perpendicularPod;

    private IMU imu;
    private ImuController imuController;

    private int startParallel;
    private int startPerpendicular;

    private double targetYaw;
    private double yawError;
    private double correction;

    private final ElapsedTime timer = new ElapsedTime();

    private boolean moving = false;
    private boolean finished = false;
    private boolean previousA = false;
    private boolean previousY = false;

    private double maxTime = 12.0;
    private String status = "Ready";

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

        motors[0].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[1].setDirection(DcMotorSimple.Direction.FORWARD);
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3].setDirection(DcMotorSimple.Direction.FORWARD);

        for (DcMotor motor : motors) {
            motor.setPower(0);
            motor.setZeroPowerBehavior(
                    DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        parallelPod = hardwareMap.get(
                DcMotor.class,
                Config.ODOMETRY_PARALLEL_ENCODER_NAME);

        perpendicularPod = hardwareMap.get(
                DcMotor.class,
                Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME);

        startParallel = -parallelPod.getCurrentPosition();
        startPerpendicular = -perpendicularPod.getCurrentPosition();

        // Use Louis's IMU controller
        imu = hardwareMap.get(IMU.class, Config.IMU_NAME);
        imuController = new ImuController(imu);

        imu.initialize(imuController.getInitializationParameters());
        imuController.resetYaw();

        telemetry.addLine("X = Drive 20 cm");
        telemetry.addLine("Triangle = Drive 100 cm");
        telemetry.addLine("Circle = Emergency stop");
        telemetry.update();
    }

    @Override
    public void loop() {

        boolean pressedA = gamepad1.a && !previousA;
        boolean pressedY = gamepad1.y && !previousY;

        previousA = gamepad1.a;
        previousY = gamepad1.y;

        if (gamepad1.b) {
            stopMotors();
            finished = true;
            status = "Stopped by driver";
        }

        if (!moving && !finished && !gamepad1.b) {

            if (pressedA) {
                startDrive(20.0);
            } else if (pressedY) {
                startDrive(100.0);
            }
        }

        if (moving) {

            boolean allFinished = true;

            for (DcMotor motor : motors) {
                if (motor.isBusy()) {
                    allFinished = false;
                }
            }

            if (allFinished) {
                stopMotors();
                finished = true;
                status = "Target reached";

            } else if (timer.seconds() >= maxTime) {
                stopMotors();
                finished = true;
                status = "Timeout - motors stopped";

            } else {
                correctHeading();
            }
        }

        telemetry.addData("Status", status);

        telemetry.addData("FL ticks",
                motors[0].getCurrentPosition());

        telemetry.addData("FR ticks",
                motors[1].getCurrentPosition());

        telemetry.addData("BL ticks",
                motors[2].getCurrentPosition());

        telemetry.addData("BR ticks",
                motors[3].getCurrentPosition());

        telemetry.addData("Yaw target", "%.2f", targetYaw);

        telemetry.addData("Yaw current", "%.2f",
                imuController.getYawInDegrees());

        telemetry.addData("Yaw error", "%.2f", yawError);
        telemetry.addData("Correction", "%.3f", correction);

        telemetry.addData("Parallel delta ticks",
                -parallelPod.getCurrentPosition() - startParallel);

        telemetry.addData("Perpendicular delta ticks",
                -perpendicularPod.getCurrentPosition()
                        - startPerpendicular);

        telemetry.addData("Elapsed (s)", "%.1f",
                timer.seconds());

        telemetry.update();
    }

    private void startDrive(double distanceCm) {

        double wheelCircumference =
                Math.PI * WHEEL_DIAMETER_CM;

        double turns = distanceCm / wheelCircumference;

        targetYaw = imuController.getYawInDegrees();
        yawError = 0;
        correction = 0;

        startParallel = -parallelPod.getCurrentPosition();
        startPerpendicular = -perpendicularPod.getCurrentPosition();

        for (int i = 0; i < motors.length; i++) {

            int ticks = (int) Math.round(
                    turns * ticksPerTurn[i]);

            // Rear motors need negative encoder targets
            ticks = ticks * forwardSigns[i];

            int targetPosition =
                    motors[i].getCurrentPosition() + ticks;

            motors[i].setTargetPosition(targetPosition);
            motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        maxTime = Math.max(12.0, distanceCm * 0.25);

        timer.reset();
        moving = true;
        status = "Driving " + distanceCm + " cm";

        for (DcMotor motor : motors) {
            motor.setPower(SPEED);
        }
    }

    private void correctHeading() {

        double currentYaw = imuController.getYawInDegrees();

        if (!Double.isFinite(currentYaw)) {
            stopMotors();
            finished = true;
            status = "IMU error - stopped";
            return;
        }

        yawError = AngleUnit.normalizeDegrees(
                currentYaw - targetYaw);

        correction = Range.clip(
                yawError * HEADING_KP,
                -MAX_CORRECTION,
                MAX_CORRECTION);

        double leftPower = Range.clip(
                SPEED + correction, 0.10, 0.34);

        double rightPower = Range.clip(
                SPEED - correction, 0.10, 0.34);

        motors[0].setPower(leftPower);
        motors[2].setPower(leftPower);

        motors[1].setPower(rightPower);
        motors[3].setPower(rightPower);
    }

    private void stopMotors() {

        moving = false;
        correction = 0;

        for (DcMotor motor : motors) {
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    @Override
    public void stop() {
        stopMotors();
    }
}
