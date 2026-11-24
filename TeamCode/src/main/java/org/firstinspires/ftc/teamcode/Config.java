
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;

import java.util.Set;

public class Config {

    public static boolean isBlueAlliance = false;

    private static final Set<Integer> redAprilTagIds =
            Set.of(1, 2, 3, 4, 5, 6, 7, 8);

    private static final Set<Integer> blueAprilTagIds =
            Set.of(9, 10, 11, 12, 13, 14, 15, 16);

    public static final double CAMERA_WIDTH = 320.0;
    public static final double CAMERA_HEIGHT = 240.0;

    public static final double APRIL_TAG_SHOOTING_POSITION_WANTED_X = 160.0;
    public static final double APRIL_TAG_SHOOTING_POSITION_WANTED_TAG_WIDTH = 18.0;
    public static final double APRIL_TAG_SHOOTING_POSITION_X_TOLERANCE = 10.0;
    public static final double APRIL_TAG_SHOOTING_POSITION_WIDTH_TOLERANCE = 6.0;
    public static final double APRIL_TAG_SHOOTING_POSITION_DRIVING_SPEED = 0.5;

    public static final String CAMERA_NAME = "huskylens";

    public static final double DRIVE_TRAIN_MAX_SPEED = 1.0;
    public static final double DRIVE_TRAIN_MAX_POWER = 1.0;
    public static final double SIDE_BALL_GRABBER_MAX_POWER = 1.0;
    public static final double DEFAULT_BALL_GRABBER_SPEED = 0.9;

    public static final String FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME = "frontLeftDrive";
    public static final String FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME = "frontRightDrive";
    public static final String BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME = "backLeftDrive";
    public static final String BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME = "backRightDrive";

    public static final String SIDE_BALL_GRABBER_LEFT_MOTOR_NAME = "sideBallGrabberLeft";
    public static final String SIDE_BALL_GRABBER_RIGHT_MOTOR_NAME = "sideBallGrabberRight";
    public static final String INTAKE_MOTOR_NAME = "intake";

    public static final Boolean DRIVE_ROBOT_RELATIVE_TO_FIELD = true;

    public static final String IMU_NAME = "imu";

    public static final UsbFacingDirection IMU_USB_FACING_DIRECTION =
            UsbFacingDirection.LEFT;

    public static final LogoFacingDirection IMU_LOGO_FACING_DIRECTION =
            LogoFacingDirection.UP;


    // ODOMETRY CONFIGURATION
    // Unconfirmed values remain NaN.

    public static final double ODOMETRY_TICKS_PER_REVOLUTION = Double.NaN;
    public static final double ODOMETRY_WHEEL_RADIUS_CM = Double.NaN;

    // Signed distances from the robot reference point.
    // Parallel offset: positive when the pod is on the left.
    // Perpendicular offset: positive when the pod is at the front.

    public static final double ODOMETRY_PARALLEL_OFFSET_CM = Double.NaN;
    public static final double ODOMETRY_PERPENDICULAR_OFFSET_CM = Double.NaN;

    public static final String ODOMETRY_PARALLEL_ENCODER_NAME = "odopody";
    public static final String ODOMETRY_PERPENDICULAR_ENCODER_NAME = "odopodx";


    public void init(Boolean isBlueAlliance) {
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
