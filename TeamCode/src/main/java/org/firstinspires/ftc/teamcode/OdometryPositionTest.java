
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "TEST - Odometry Position", group = "Tests")
public class OdometryPositionTest extends OpMode {

    private IMU imu;
    private OdometrySensors sensors;
    private Odometry odometry;

    private String status = "Not configured";

    @Override
    public void init() {

        // Initialize the IMU
        imu = hardwareMap.get(IMU.class, Config.IMU_NAME);

        RevHubOrientationOnRobot orientation =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                );

        imu.initialize(new IMU.Parameters(orientation));

        // Check odopod names
        if (Config.ODOMETRY_PARALLEL_ENCODER_NAME.isEmpty()
                || Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME.isEmpty()) {

            status = "Odopod names are missing in Config";
            return;
        }

        // Connect the two encoders and the IMU
        try {
            sensors = new OdometrySensors(
                    hardwareMap,
                    Config.ODOMETRY_PARALLEL_ENCODER_NAME,
                    Config.ODOMETRY_PERPENDICULAR_ENCODER_NAME,
                    Config.IMU_NAME,
                    true,  // X: reversed
                    true   // Y: reversed
            );
        } catch (RuntimeException e) {
            status = "Odopods not detected: " + e.getMessage();
            return;
        }

        // Check calibration values
        if (!calibrationReady()) {
            status = "Encoder ticks only - calibration missing";
            return;
        }

        odometry = new Odometry(
                Config.ODOMETRY_TICKS_PER_REVOLUTION,
                Config.ODOMETRY_WHEEL_RADIUS_CM,
                Config.ODOMETRY_PARALLEL_OFFSET_CM,
                Config.ODOMETRY_PERPENDICULAR_OFFSET_CM
        );

        status = "Odometry ready";
    }

    @Override
    public void start() {

        // Set the starting position to zero
        if (sensors != null && odometry != null) {
            sensors.resetOdometry(odometry);
        }
    }

    @Override
    public void loop() {

        // Show the IMU heading
        telemetry.addData(
                "IMU Heading",
                "%.2f deg",
                imu.getRobotYawPitchRollAngles()
                        .getYaw(AngleUnit.DEGREES)
        );

        if (sensors != null) {

            telemetry.addData(
                    "Parallel Ticks",
                    sensors.getParallelTicks()
            );

            telemetry.addData(
                    "Perpendicular Ticks",
                    sensors.getPerpendicularTicks()
            );
        }

        if (sensors != null && odometry != null) {

            sensors.updateOdometry(odometry);

            telemetry.addData("X (cm)", "%.2f", odometry.getXcm());
            telemetry.addData("Y (cm)", "%.2f", odometry.getYcm());
            telemetry.addData(
                    "Heading (deg)",
                    "%.2f",
                    odometry.getHeadingDegrees()
            );

        } else {
            telemetry.addData("X / Y", "Not available");
        }

        telemetry.addData("Status", status);
        telemetry.update();
    }

    private boolean calibrationReady() {
        return Double.isFinite(Config.ODOMETRY_TICKS_PER_REVOLUTION)
                && Config.ODOMETRY_TICKS_PER_REVOLUTION > 0
                && Double.isFinite(Config.ODOMETRY_WHEEL_RADIUS_CM)
                && Config.ODOMETRY_WHEEL_RADIUS_CM > 0
                && Double.isFinite(Config.ODOMETRY_PARALLEL_OFFSET_CM)
                && Double.isFinite(Config.ODOMETRY_PERPENDICULAR_OFFSET_CM);
    }
}
