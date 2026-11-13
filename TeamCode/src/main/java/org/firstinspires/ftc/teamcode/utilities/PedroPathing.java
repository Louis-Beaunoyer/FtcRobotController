package org.firstinspires.ftc.teamcode.utilities;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.CustomIMU;
import com.pedropathing.revhub.localizers.Encoder;
import com.pedropathing.revhub.localizers.TwoWheelConfig;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Config;

public class PedroPathing {

    private final Follower follower;

    // Directions tested with our Mecanum diagnostic.
    public static MecanumConfig createMecanumConfig() {

        return new MecanumConfig(c -> {

            c.frontLeftName.set(
                    Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);

            c.frontRightName.set(
                    Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

            c.backLeftName.set(
                    Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);

            c.backRightName.set(
                    Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

            c.frontLeftDirection.set(
                    DcMotorSimple.Direction.REVERSE);

            c.frontRightDirection.set(
                    DcMotorSimple.Direction.FORWARD);

            c.backLeftDirection.set(
                    DcMotorSimple.Direction.FORWARD);

            c.backRightDirection.set(
                    DcMotorSimple.Direction.REVERSE);
        });
    }

    // Two-wheel localization configuration.
    // All geometric distances are in inches.
    public static TwoWheelConfig createTwoWheelConfig(
            String parallelName,
            String perpendicularName,
            double forwardTicksToInches,
            double strafeTicksToInches,
            double xPodOffset,
            double yPodOffset,
            boolean reverseParallel,
            boolean reversePerpendicular,
            CustomIMU imu) {

        if (parallelName == null || parallelName.isEmpty()
                || perpendicularName == null
                || perpendicularName.isEmpty()
                || imu == null) {

            throw new IllegalArgumentException(
                    "Odometry hardware not configured");
        }

        if (!Double.isFinite(forwardTicksToInches)
                || !Double.isFinite(strafeTicksToInches)
                || forwardTicksToInches <= 0
                || strafeTicksToInches <= 0
                || !Double.isFinite(xPodOffset)
                || !Double.isFinite(yPodOffset)) {

            throw new IllegalArgumentException(
                    "Invalid odometry calibration");
        }

        return new TwoWheelConfig(c -> {

            c.xPodName.set(parallelName);
            c.yPodName.set(perpendicularName);

            c.imuName.set(Config.IMU_NAME);

            c.forwardTicksToInches.set(
                    forwardTicksToInches);

            c.strafeTicksToInches.set(
                    strafeTicksToInches);

            c.xPodOffset.set(xPodOffset);
            c.yPodOffset.set(yPodOffset);

            c.xPodDirection.set(
                    reverseParallel
                            ? Encoder.REVERSE
                            : Encoder.FORWARD);

            c.yPodDirection.set(
                    reversePerpendicular
                            ? Encoder.REVERSE
                            : Encoder.FORWARD);

            c.imu.set(imu);
        });
    }

    // New convenience factory.
    // Uses our shared physical odometry calibration.
    public static Follower createFollower(
            HardwareMap hardwareMap,
            ForesightConfig pathConfig) {

        return createFollower(
                hardwareMap,
                PedroLocalizationConfig.create(),
                pathConfig
        );
    }

    // Original factory retained for compatibility.
    public static Follower createFollower(
            HardwareMap hardwareMap,
            TwoWheelConfig odometryConfig,
            ForesightConfig pathConfig) {

        if (hardwareMap == null
                || odometryConfig == null
                || pathConfig == null) {

            throw new IllegalArgumentException(
                    "Follower configuration is missing");
        }

        TwoWheelLocalizer localizer =
                new TwoWheelLocalizer(
                        hardwareMap,
                        odometryConfig);

        Mecanum drivetrain =
                new Mecanum(
                        hardwareMap,
                        createMecanumConfig());

        Foresight algorithm =
                new Foresight(pathConfig);

        return new Follower(
                localizer,
                drivetrain,
                algorithm);
    }

    public PedroPathing(Follower follower) {

        if (follower == null) {
            throw new IllegalArgumentException(
                    "Follower cannot be null");
        }

        this.follower = follower;
    }

    public void update() {
        follower.update();
    }

    public void followPath(Path path) {

        if (path == null) {
            throw new IllegalArgumentException(
                    "Path cannot be null");
        }

        follower.follow(path);
    }

    // Cancel Pedro and request a drivetrain stop.
    // The central direct-motor stop is still required
    // when changing between manual and auto control.
    public void stop() {

        follower.stop();
        follower.drivetrain.stop();
    }

    public Pose getPose() {
        return follower.pose();
    }

    public boolean isBusy() {
        return follower.isBusy();
    }
}
