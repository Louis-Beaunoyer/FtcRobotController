package org.firstinspires.ftc.teamcode;
import java.util.Set;

public class Config {

    public static boolean isBlueAlliance;
    private static final Set<Integer> redAprilTagIds = Set.of(0, 1, 2, 3, 4, 5, 6, 7);
    private static final Set<Integer> blueAprilTagIds = Set.of(34, 35, 36, 37, 38, 39, 40, 41);

    public static final int CAMERA_WIDTH = 320;
    public static final int CAMERA_HEIGHT = 240;
    public static final double tagWidthFromShootingDistance = 0.0;
    public static final double tagHeightFromShootingDistance = 0.0;
    public static final double driveTrainMaxSpeed = 1.0;
    public static final String FrontLeftDriveTrainMotorName = "frontLeftDrive";
    public static final String FrontRightDriveTrainMotorName = "frontRightDrive";
    public static final String BackLeftDriveTrainMotorName = "backLeftDrive";
    public static final String BackRightDriveTrainMotorName = "backRightDrive";
    public static final String IntakeMotorName = "intake";





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
