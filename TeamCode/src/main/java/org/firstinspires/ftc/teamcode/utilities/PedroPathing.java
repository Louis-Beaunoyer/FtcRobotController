
package org.firstinspires.ftc.teamcode.utilities;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class PedroPathing {

    private final Follower follower;

    // The follower is configured by the main robot code
    public PedroPathing(Follower follower) {
        this.follower = follower;
    }

    // Update the robot position and path following
    public void update() {
        follower.update();
    }

    // Start follow a path
    public void followPath(Path path) {
        follower.follow(path);
    }

    // Stop following the live path
    public void stop() {
        follower.stop();
        follower.update();
    }

    // tack the robot position
    public Pose getPose() {
        return follower.pose();
    }

    // Check if the robot is following the path
    public boolean isBusy() {
        return follower.isBusy();
    }
}
