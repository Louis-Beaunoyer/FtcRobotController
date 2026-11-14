package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.utilities.PedroLocalizationConfig;

@TeleOp(name = "TEST - Pedro TwoWheel Position", group = "Tests")
public class PedroTwoWheelPositionTest extends OpMode {

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

    // Verify odopod connections before initialization.
    private void validateHardware() {

        parallelEncoder = hardwareMap.get(
                DcMotorEx.class,
                Config.ODOMETRY_PARALLEL_ENCODER_NAME
        );

        perpendicularEncoder = hardwareMap.get(
                DcMotorEx.class,
                Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME
        );

        hardwareMap.get(IMU.class, Config.IMU_NAME);

        if (parallelEncoder == perpendicularEncoder) {
            throw new IllegalStateException(
                    "Both odopods use the same motor port"
            );
        }

        String[] motorNames = {
                Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME,
                Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME,
                Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME,
                Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME,
                Config.INTAKE_MOTOR_NAME
        };

        for (String name : motorNames) {

            DcMotorEx motor = hardwareMap.get(
                    DcMotorEx.class,
                    name
            );

            if (parallelEncoder == motor
                    || perpendicularEncoder == motor) {

                throw new IllegalStateException(
                        "Odopod port shared with motor: " + name
                );
            }
        }

        // Preserve the previously tested SDK directions.
        // Pedro handles its own encoder reversal.
        if (parallelEncoder.getDirection()
                != DcMotorSimple.Direction.FORWARD
                || perpendicularEncoder.getDirection()
                != DcMotorSimple.Direction.FORWARD) {

            throw new IllegalStateException(
                    "Odopod SDK direction is not FORWARD"
            );
        }
    }

    @Override
    public void start() {

        if (!hardwareReady) {
            return;
        }

        try {
            validateHardware();

            // Single shared odometry configuration.
            // Uses the official Pedro TwoWheelLocalizer.
            localizer = new TwoWheelLocalizer(
                    hardwareMap,
                    PedroLocalizationConfig.create()
            );

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
            // Update localization only.
            // No Follower or drivetrain commands.
            localizer.update();

            Pose pose = localizer.pose();

            double xCm = pose.x() * CM_PER_INCH;
            double yCm = pose.y() * CM_PER_INCH;
            double heading = Math.toDegrees(
                    pose.heading()
            );

            // Display heading between -180 and +180.
            if (heading > 180.0) {
                heading -= 360.0;
            }

            if (!Double.isFinite(xCm)
                    || !Double.isFinite(yCm)
                    || !Double.isFinite(heading)) {

                throw new IllegalStateException(
                        "Invalid Pedro position"
                );
            }

            telemetry.addData(
                    "Pedro X (cm)", "%.2f", xCm
            );

            telemetry.addData(
                    "Pedro Y (cm)", "%.2f", yCm
            );

            telemetry.addData(
                    "Pedro Heading (deg)",
                    "%.2f", heading
            );

            telemetry.addData(
                    "Parallel raw ticks",
                    parallelEncoder.getCurrentPosition()
            );

            telemetry.addData(
                    "Perpendicular raw ticks",
                    perpendicularEncoder.getCurrentPosition()
            );

            telemetry.addLine(
                    "Offsets: PedroLocalizationConfig"
            );

            telemetry.addLine(
                    "Physical offsets are provisional"
            );

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

        // No drivetrain or Follower was created.
        // This OpMode never commands propulsion.
        localizer = null;
    }
}
