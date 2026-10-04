package org.firstinspires.ftc.teamcode;
import java.util.Set;

public class Config {

    public static boolean isBlueAlliance;
    private static final Set<Integer> redAprilTagIds = Set.of(0, 1, 2, 3, 4, 5, 6, 7);
    private static final Set<Integer> blueAprilTagIds = Set.of(34, 35, 36, 37, 38, 39, 40, 41);


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
