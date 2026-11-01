package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;

import java.util.Set;

public class Config {

    public static boolean isBlueAlliance;
    private static final Set<Integer> redAprilTagIds = Set.of(0, 1, 2, 3, 4, 5, 6, 7);
    private static final Set<Integer> blueAprilTagIds = Set.of(34, 35, 36, 37, 38, 39, 40, 41);

    public static final int CAMERA_WIDTH = 320;
    public static final int CAMERA_HEIGHT = 240;
    public static final double TAG_WIDTH_FROM_SHOOTING_DISTANCE = 0.0;
    public static final double TAG_HEIGHT_FROM_SHOOTING_DISTANCE = 0.0;
    public static final double DRIVE_TRAIN_MAX_SPEED = 1.0;
    public static final double DRIVE_TRAIN_MAX_POWER = 1.0;
    public static final String FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME = "frontLeftDrive";
    public static final String FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME = "frontRightDrive";
    public static final String BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME = "backLeftDrive";
    public static final String BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME = "backRightDrive";
    public static final String INTAKE_MOTOR_NAME = "intake";
    public static final Boolean DRIVE_ROBOT_RELATIVE_TO_FIELD = true;
    public static final String IMU_NAME = "imu";

    public static final UsbFacingDirection IMU_USB_FACING_DIRECTION = UsbFacingDirection.FORWARD;
    public static final LogoFacingDirection IMU_LOGO_FACING_DIRECTION = LogoFacingDirection.RIGHT;






    public Config(boolean isBlueAlliance) {
        Config.isBlueAlliance = isBlueAlliance;

    }

    public static Set<Integer> getIds() {
        if (isBlueAlliance) {
            return blueAprilTagIds;
        } else {
            return redAprilTagIds;
        }


    }
}
