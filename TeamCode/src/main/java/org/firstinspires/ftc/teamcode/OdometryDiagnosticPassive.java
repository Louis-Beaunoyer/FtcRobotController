package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.utilities.ImuController;

@TeleOp(name = "TEST - Odometry Passive Diagnostic", group = "Tests")
public class OdometryDiagnosticPassive extends OpMode {

    // Calibrations from our previous robot tests
    private static final double PARALLEL_TICKS_PER_CM = 198.21;
    private static final double PERPENDICULAR_TICKS_PER_CM = 198.07;

    // Provisional physical offsets
    // Parallel pod is to the right of robot center
    private static final double PARALLEL_OFFSET_CM = -15.8;

    // Perpendicular pod is behind robot center
    private static final double PERPENDICULAR_OFFSET_CM = -11.3;

    // Encoder directions used in our previous tests
    private static final boolean REVERSE_PARALLEL = true;
    private static final boolean REVERSE_PERPENDICULAR = true;

    private DcMotor parallelEncoder;
    private DcMotor perpendicularEncoder;
    private OdometrySensors sensors;

    private boolean initialized = false;
    private boolean paused = false;
    private boolean previousA = false;

    private int originParallel;
    private int originPerpendicular;
    private int previousParallel;
    private int previousPerpendicular;

    private double previousYaw;
    private double xCm = 0;
    private double yCm = 0;
    private double headingRadians = 0;

    private String status = "Not initialized";

    @Override
    public void init() {
        try {
            parallelEncoder = hardwareMap.get(
                    DcMotor.class,
                    Config.ODOMETRY_PARALLEL_ENCODER_NAME);

            perpendicularEncoder = hardwareMap.get(
                    DcMotor.class,
                    Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME);

            IMU imu = hardwareMap.get(
                    IMU.class, Config.IMU_NAME);

            // Use Louis's IMU controller and orientation
            ImuController imuController = new ImuController(imu);
            imu.initialize(imuController.getInitializationParameters());

            sensors = new OdometrySensors(
                    hardwareMap,
                    Config.ODOMETRY_PARALLEL_ENCODER_NAME,
                    Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME,
                    Config.IMU_NAME,
                    REVERSE_PARALLEL,
                    REVERSE_PERPENDICULAR);

            status = "Ready - passive test";

        } catch (RuntimeException e) {
            sensors = null;
            status = "Initialization error: " + e.getMessage();
        }

        telemetry.addData("Status", status);
        telemetry.addLine("No motor commands");
        telemetry.addLine("PlayStation X = reset position");
        telemetry.update();
    }

    @Override
    public void start() {
        initialized = false;
        paused = false;
        previousA = false;
    }

    @Override
    public void loop() {

        if (sensors == null) {
            telemetry.addData("Status", status);
            telemetry.update();
            return;
        }

        try {
            int rawParallel = parallelEncoder.getCurrentPosition();
            int rawPerpendicular =
                    perpendicularEncoder.getCurrentPosition();

            int parallelTicks = sensors.getParallelTicks();
            int perpendicularTicks =
                    sensors.getPerpendicularTicks();

            double yaw = sensors.getHeadingRadians();

            if (!Double.isFinite(yaw)) {
                paused = true;
                status = "Invalid IMU - position frozen";
                telemetry.addData("Status", status);
                telemetry.update();
                return;
            }

            boolean pressedA = gamepad1.a && !previousA;
            previousA = gamepad1.a;

            if (!initialized || pressedA) {

                resetPosition(
                        parallelTicks,
                        perpendicularTicks,
                        yaw);

                status = "Position reset";

            } else if (paused) {

                previousParallel = parallelTicks;
                previousPerpendicular = perpendicularTicks;
                previousYaw = yaw;

                paused = false;
                status = "Sensors restored";

            } else {

                updatePosition(
                        parallelTicks,
                        perpendicularTicks,
                        yaw);

                status = "Reading OK";
            }

            telemetry.addData("Status", status);

            telemetry.addData("Parallel raw ticks", rawParallel);
            telemetry.addData(
                    "Perpendicular raw ticks", rawPerpendicular);

            telemetry.addData(
                    "Parallel signed ticks", parallelTicks);

            telemetry.addData(
                    "Perpendicular signed ticks", perpendicularTicks);

            telemetry.addData(
                    "Parallel distance (cm)", "%.2f",
                    ((long) parallelTicks - originParallel)
                            / PARALLEL_TICKS_PER_CM);

            telemetry.addData(
                    "Perpendicular distance (cm)", "%.2f",
                    ((long) perpendicularTicks - originPerpendicular)
                            / PERPENDICULAR_TICKS_PER_CM);

            telemetry.addData(
                    "IMU yaw (deg)", "%.2f", Math.toDegrees(yaw));

            telemetry.addData("X (cm)", "%.2f", xCm);
            telemetry.addData("Y (cm)", "%.2f", yCm);

            telemetry.addData(
                    "Heading (deg)", "%.2f",
                    Math.toDegrees(headingRadians));

            telemetry.addData(
                    "Parallel offset (cm)", "%.2f",
                    PARALLEL_OFFSET_CM);

            telemetry.addData(
                    "Perpendicular offset (cm)", "%.2f",
                    PERPENDICULAR_OFFSET_CM);

            telemetry.addLine("Offsets are PROVISIONAL");
            telemetry.addLine("PlayStation X = reset software position");
            telemetry.addLine("Motors are never commanded");

            telemetry.update();

        } catch (RuntimeException e) {
            paused = true;
            status = "Sensor error: " + e.getMessage();
            telemetry.addData("Status", status);
            telemetry.update();
        }
    }

    private void resetPosition(
            int parallelTicks,
            int perpendicularTicks,
            double yaw) {

        originParallel = parallelTicks;
        originPerpendicular = perpendicularTicks;

        previousParallel = parallelTicks;
        previousPerpendicular = perpendicularTicks;
        previousYaw = yaw;

        xCm = 0;
        yCm = 0;
        headingRadians = 0;

        initialized = true;
        paused = false;
    }

    private void updatePosition(
            int parallelTicks,
            int perpendicularTicks,
            double yaw) {

        double deltaHeading = Math.atan2(
                Math.sin(yaw - previousYaw),
                Math.cos(yaw - previousYaw));

        double deltaParallel =
                ((long) parallelTicks - previousParallel)
                        / PARALLEL_TICKS_PER_CM;

        double deltaPerpendicular =
                ((long) perpendicularTicks - previousPerpendicular)
                        / PERPENDICULAR_TICKS_PER_CM;

        // Remove movement caused by robot rotation
        double forward =
                deltaParallel
                        + PARALLEL_OFFSET_CM * deltaHeading;

        double left =
                deltaPerpendicular
                        - PERPENDICULAR_OFFSET_CM * deltaHeading;

        // Correct movement during simultaneous rotation
        double halfTurn = deltaHeading / 2.0;
        double arcScale;

        if (Math.abs(halfTurn) < 0.0001) {
            double square = halfTurn * halfTurn;
            arcScale = 1.0 - square / 6.0
                    + square * square / 120.0;
        } else {
            arcScale = Math.sin(halfTurn) / halfTurn;
        }

        double middleHeading =
                headingRadians + halfTurn;

        // Robot coordinates: X forward, Y left
        xCm += arcScale * (
                forward * Math.cos(middleHeading)
                        - left * Math.sin(middleHeading));

        yCm += arcScale * (
                forward * Math.sin(middleHeading)
                        + left * Math.cos(middleHeading));

        headingRadians += deltaHeading;

        previousParallel = parallelTicks;
        previousPerpendicular = perpendicularTicks;
        previousYaw = yaw;
    }

    @Override
    public void stop() {
        // No motors to stop: this OpMode only reads sensors
    }
}
