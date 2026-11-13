package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.RevHubIMU;
import com.pedropathing.revhub.localizers.TwoWheelConfig;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.utilities.PedroPathing;

@TeleOp(name = "TEST - Pedro TwoWheel Position", group = "Tests")
public class PedroTwoWheelPositionTest extends OpMode {

    // Calibrations measured on our robot
    private static final double PARALLEL_TICKS_PER_CM = 198.21;
    private static final double PERPENDICULAR_TICKS_PER_CM = 198.07;

    // Provisional physical offsets in our robot convention
    private static final double PARALLEL_OFFSET_CM = -15.8;
    private static final double PERPENDICULAR_OFFSET_CM = -11.3;

    private static final double CM_PER_INCH = 2.54;

    private TwoWheelLocalizer localizer;

    private DcMotorEx parallelEncoder;
    private DcMotorEx perpendicularEncoder;

    private boolean hardwareReady = false;
    private String status = "Not initialized";

    @Override
    public void init() {

        try {
            validateHardware();
            hardwareReady = true;
            status = "Ready - press PLAY";

        } catch (RuntimeException e) {
            hardwareReady = false;
            status = "Hardware error: " + e.getMessage();
        }

        telemetry.addData("Status", status);
        telemetry.addLine("Pedro TwoWheel localization test");
        telemetry.addLine("No drive motor commands");
        telemetry.addLine("PLAY will reset the two odopod encoders");
        telemetry.update();
    }

    // Verify that odopod ports are not drivetrain ports
    private void validateHardware() {

        parallelEncoder = hardwareMap.get(
                DcMotorEx.class,
                Config.ODOMETRY_PARALLEL_ENCODER_NAME);

        perpendicularEncoder = hardwareMap.get(
                DcMotorEx.class,
                Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME);

        hardwareMap.get(IMU.class, Config.IMU_NAME);

        if (parallelEncoder == perpendicularEncoder) {
            throw new IllegalStateException(
                    "Both odopods use the same motor port");
        }

        String[] driveMotorNames = {
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME,
                Config.INTAKE_MOTOR_NAME
        };

        for (String name : driveMotorNames) {

            DcMotorEx motor = hardwareMap.get(DcMotorEx.class, name);

            if (parallelEncoder == motor ||
                    perpendicularEncoder == motor) {

                throw new IllegalStateException(
                        "Odopod port shared with motor: " + name);
            }
        }

        // Pedro Encoder already accounts for SDK direction.
        // Our previous signed readings used negative raw ticks.
        if (parallelEncoder.getDirection()
                != DcMotorSimple.Direction.FORWARD ||
                perpendicularEncoder.getDirection()
                != DcMotorSimple.Direction.FORWARD) {

            throw new IllegalStateException(
                    "Odopod SDK direction is not FORWARD");
        }
    }

    @Override
    public void start() {

        if (!hardwareReady) {
            return;
        }

        try {
            validateHardware();

            // Same physical IMU orientation as Louis's Config
            RevHubOrientationOnRobot orientation =
                    new RevHubOrientationOnRobot(
                            Config.IMU_LOGO_FACING_DIRECTION,
                            Config.IMU_USB_FACING_DIRECTION);

            RevHubIMU pedroImu = new RevHubIMU(orientation);

            // Pedro uses inches per tick
            double forwardTicksToInches =
                    1.0 / (PARALLEL_TICKS_PER_CM * CM_PER_INCH);

            double strafeTicksToInches =
                    1.0 / (PERPENDICULAR_TICKS_PER_CM * CM_PER_INCH);

            // Pedro 3.0.2 has a different parallel offset sign
            double xPodOffset =
                    -PARALLEL_OFFSET_CM / CM_PER_INCH;

            double yPodOffset =
                    PERPENDICULAR_OFFSET_CM / CM_PER_INCH;

            // Reuse our existing PedroPathing configuration
            TwoWheelConfig odometryConfig =
                    PedroPathing.createTwoWheelConfig(
                            Config.ODOMETRY_PARALLEL_ENCODER_NAME,
                            Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME,
                            forwardTicksToInches,
                            strafeTicksToInches,
                            xPodOffset,
                            yPodOffset,
                            true,
                            true,
                            pedroImu
                    );

            // This initializes and resets ONLY the odopod ports.
            // It also initializes the IMU.
            // No Mecanum drivetrain or Follower is created.
            localizer = new TwoWheelLocalizer(
                    hardwareMap, odometryConfig);

            status = "Pedro localization running";

        } catch (RuntimeException e) {
            localizer = null;
            status = "Start error: " + e.getMessage();
        }
    }

    @Override
    public void loop() {

        telemetry.addData("Status", status);

        if (localizer == null) {
            telemetry.addLine("Localization unavailable");
            telemetry.update();
            return;
        }

        try {
            // Update only the localizer, not the Follower
            localizer.update();

            Pose pose = localizer.pose();

            double xCm = pose.x() * CM_PER_INCH;
            double yCm = pose.y() * CM_PER_INCH;
            double heading = Math.toDegrees(pose.heading());

            // Display heading between -180 and +180 degrees
            if (heading > 180.0) {
                heading -= 360.0;
            }

            if (!Double.isFinite(xCm)
                    || !Double.isFinite(yCm)
                    || !Double.isFinite(heading)) {

                throw new IllegalStateException(
                        "Invalid Pedro position");
            }

            telemetry.addData("Pedro X (cm)", "%.2f", xCm);
            telemetry.addData("Pedro Y (cm)", "%.2f", yCm);
            telemetry.addData("Pedro Heading (deg)", "%.2f", heading);

            telemetry.addData("Parallel raw ticks",
                    parallelEncoder.getCurrentPosition());

            telemetry.addData("Perpendicular raw ticks",
                    perpendicularEncoder.getCurrentPosition());

            telemetry.addData("Parallel offset (cm)",
                    "%.2f", PARALLEL_OFFSET_CM);

            telemetry.addData("Perpendicular offset (cm)",
                    "%.2f", PERPENDICULAR_OFFSET_CM);

            telemetry.addLine("Offsets are provisional");
            telemetry.addLine("X forward / Y left");
            telemetry.addLine("No motor commands");

        } catch (RuntimeException e) {

            localizer = null;
            status = "Localization error: " + e.getMessage();
            telemetry.addData("Status", status);
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        // No motor controller or Follower was created.
        // Nothing commands propulsion in this test.
        localizer = null;
    }
}
