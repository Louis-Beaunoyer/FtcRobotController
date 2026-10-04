package org.firstinspires.ftc.teamcode;

import android.util.Pair;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.List;
import java.util.Set;

public class AprilTagMovement {

    public AprilTagMovement(double shootingPositionX, double shootingPositionZ) {
        this.shootingPositionX = shootingPositionX;
        this.shootingPositionZ = shootingPositionZ;
        AprilTagIds = Config.getIds();
    }

    double AprilTagX;
    double AprilTagZ;
    boolean left;
    boolean right;
    boolean forwards;
    boolean back;
    double shootingPositionX;
    double shootingPositionZ;
    Set<Integer> AprilTagIds;


    public void DirectionFinder(AprilTagProcessor aprilTag, TriFunction<Double, Double, Double, String> driveFunction) { //replace by actual opmode instead of sample opmode
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        for (AprilTagDetection detection : currentDetections) {
            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;

                if ((singleDet.id == 23) || (singleDet.id == 24)) {
                    AprilTagX = singleDet.robotPose.getPosition().x;
                    AprilTagZ = singleDet.robotPose.getPosition().z;


                    //move right
                    for (int i = 0; i < 5; i++) {
                        driveFunction.apply(1.0, 1.0, 0.0);
                    }

                    // forward

                    if (singleDet.robotPose.getPosition().x > AprilTagX) {
                        left = true;
                        right = false;

                    } else {
                        left = false;
                        right = true;
                    }


                    if (singleDet.robotPose.getPosition().z > AprilTagZ) {
                        back = true;
                        forwards = false;
                    } else {
                        back = false;
                        forwards = true;

                    }
                    AprilTagX = singleDet.robotPose.getPosition().x;
                    AprilTagZ = singleDet.robotPose.getPosition().z;
                }


            }

        }


    }

    Pair<Double, Double> findDistanceAndAngle() {

        double r;
        double theta;
        r = Math.pow(shootingPositionZ, 2) + Math.pow(shootingPositionX, 2);
        r = Math.sqrt(r);
        theta = Math.atan((shootingPositionZ / shootingPositionX));
        double newForwards = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);
        if (this.left) {newRight = -1 * newRight;}
        if (this.back) {newForwards = -1 * newForwards;}
        return new Pair<>(newForwards, newRight);
    }

    public void goToShootingPosition(TriFunction<Double, Double, Double, String> driveFunction, AprilTagProcessor aprilTag) {
        DirectionFinder(aprilTag, driveFunction);

        Pair<Double, Double> parameters = findDistanceAndAngle();
        double newForwards = parameters.first;
        double newRight = parameters.second;
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        int i = 0;
        while (AprilTagZ != shootingPositionZ && AprilTagX != shootingPositionX) {
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;

                    if (AprilTagIds.contains(singleDet.id)) {
                        AprilTagX = singleDet.robotPose.getPosition().x;
                        AprilTagZ = singleDet.robotPose.getPosition().z;
                        break;
                    }else {

                        if (this.left) {
                            driveFunction.apply(0.0, 0.0, 0.5);
                        } else {
                            driveFunction.apply(0.0, 0.0, -0.5);
                        }
                    }

                }
            }
            if (i % 4 == 0) {
                driveFunction.apply(newForwards, newRight, 0.0);
            }
            i++;
            if (AprilTagZ != shootingPositionZ && AprilTagX != shootingPositionX) {
                break;
            }
        }
    }
}

