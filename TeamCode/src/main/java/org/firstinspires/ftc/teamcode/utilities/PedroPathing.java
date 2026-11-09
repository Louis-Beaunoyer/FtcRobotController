
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

    // Configure the four mecanum motors
    public static MecanumConfig createMecanumConfig() {
        return new MecanumConfig(c -> {
            c.frontLeftName.set(Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);
            c.frontRightName.set(Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);
            c.backLeftName.set(Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);
            c.backRightName.set(Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);

            c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
            c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
            c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
            c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        });
    }

    // Configure two odometry wheels and the IMU
    // Distances must be in inches
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
                || perpendicularName == null || perpendicularName.isEmpty()
                || imu == null) {
            throw new IllegalArgumentException("Odometry hardware not configured");
        }

        if (!Double.isFinite(forwardTicksToInches)
                || !Double.isFinite(strafeTicksToInches)
                || forwardTicksToInches <= 0
                || strafeTicksToInches <= 0
                || !Double.isFinite(xPodOffset)
                || !Double.isFinite(yPodOffset)) {
            throw new IllegalArgumentException("Invalid odometry calibration");
        }

        return new TwoWheelConfig(c -> {
            c.xPodName.set(parallelName);
            c.yPodName.set(perpendicularName);
            c.imuName.set(Config.IMU_NAME);

            c.forwardTicksToInches.set(forwardTicksToInches);
            c.strafeTicksToInches.set(strafeTicksToInches);

            c.xPodOffset.set(xPodOffset);
            c.yPodOffset.set(yPodOffset);

            c.xPodDirection.set(
                    reverseParallel ? Encoder.REVERSE : Encoder.FORWARD);

            c.yPodDirection.set(
                    reversePerpendicular ? Encoder.REVERSE : Encoder.FORWARD);

            c.imu.set(imu);
        });
    }

    // Create the follower using the configured hardware
    public static Follower createFollower(
            HardwareMap hardwareMap,
            TwoWheelConfig odometryConfig,
            ForesightConfig pathConfig) {

        TwoWheelLocalizer localizer =
                new TwoWheelLocalizer(hardwareMap, odometryConfig);

        Mecanum drivetrain =
                new Mecanum(hardwareMap, createMecanumConfig());

        Foresight algorithm = new Foresight(pathConfig);

        return new Follower(localizer, drivetrain, algorithm);
    }

    // Receive the configured follower
    public PedroPathing(Follower follower) {
        this.follower = follower;
    }

    // Update localization and path following
    public void update() {
        follower.update();
    }

    // Follow a path
    public void followPath(Path path) {
        follower.follow(path);
    }

    // Stop following the path
    public void stop() {
        follower.stop();
        follower.update();
    }

    // Get the robot position
    public Pose getPose() {
        return follower.pose();
    }

    // Check if the follower is busy
    public boolean isBusy() {
        return follower.isBusy();
    }
}
