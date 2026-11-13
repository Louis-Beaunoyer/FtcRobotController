package org.firstinspires.ftc.teamcode.utilities;

import com.pedropathing.revhub.localizers.RevHubIMU;
import com.pedropathing.revhub.localizers.TwoWheelConfig;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

import org.firstinspires.ftc.teamcode.Config;

public final class PedroLocalizationConfig {

    // Measured odopod calibration.
    private static final double PARALLEL_TICKS_PER_CM = 198.21;
    private static final double PERPENDICULAR_TICKS_PER_CM = 198.07;

    // Provisional physical offsets.
    // Robot convention: left and forward are positive.
    private static final double PARALLEL_OFFSET_CM = -15.8;
    private static final double PERPENDICULAR_OFFSET_CM = -11.3;

    private static final double CM_PER_INCH = 2.54;

    private PedroLocalizationConfig() {
        // Utility class.
    }

    public static TwoWheelConfig create() {

        // Use Louis's existing IMU configuration.
        RevHubOrientationOnRobot orientation =
                new RevHubOrientationOnRobot(
                        Config.IMU_LOGO_FACING_DIRECTION,
                        Config.IMU_USB_FACING_DIRECTION
                );

        RevHubIMU pedroImu = new RevHubIMU(orientation);

        // Pedro requires inches per encoder tick.
        double forwardTicksToInches =
                1.0 / (PARALLEL_TICKS_PER_CM * CM_PER_INCH);

        double strafeTicksToInches =
                1.0 / (PERPENDICULAR_TICKS_PER_CM * CM_PER_INCH);

        // Pedro's parallel offset uses the opposite sign.
        double xPodOffset =
                -PARALLEL_OFFSET_CM / CM_PER_INCH;

        double yPodOffset =
                PERPENDICULAR_OFFSET_CM / CM_PER_INCH;

        // Reuse our existing Pedro configuration factory.
        return PedroPathing.createTwoWheelConfig(
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
    }
}
