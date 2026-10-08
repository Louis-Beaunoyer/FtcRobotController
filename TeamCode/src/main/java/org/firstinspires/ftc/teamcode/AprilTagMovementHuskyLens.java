package org.firstinspires.ftc.teamcode;

import android.util.Pair;



import com.qualcomm.hardware.dfrobot.HuskyLens;

import java.util.Set;

public class AprilTagMovementHuskyLens {

    public AprilTagMovementHuskyLens(double shootingPositionX, double shootingPositionZ) {
        this.shootingPositionX = shootingPositionX;
        this.shootingPositionY = shootingPositionZ;
        huskyLensIds = Config.getIds();
    }

    double huskyLensX;
    double huskyLensY;
    boolean left;
    boolean right;
    boolean forwards;
    boolean back;
    double shootingPositionX;
    double shootingPositionY;
    Set<Integer> huskyLensIds;


    public void DirectionFinder(HuskyLens huskyLens, TriFunction<Double, Double, Double, String> driveFunction) { //replace by actual opmode instead of sample opmode
        HuskyLens.Block[] blocks = huskyLens.blocks();
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] != null) {

                if ((blocks[i].id == 23) || (blocks[i].id == 24)) {
                    huskyLensX = blocks[i].x;
                    huskyLensY = blocks[i].y;


                    //move right
                    for (int e = 0; e < 3; e++) {
                        driveFunction.apply(1.0, 1.0, 0.0);
                    }

                    // forward

                    if (blocks[i].x > huskyLensX) {
                        left = true;
                        right = false;

                    } else {
                        left = false;
                        right = true;
                    }


                    if (blocks[i].y > huskyLensY) {
                        back = true;
                        forwards = false;
                    } else {
                        back = false;
                        forwards = true;

                    }
                    huskyLensX = blocks[i].x;
                    huskyLensY = blocks[i].y;
                }


            }

        }


    }

    Pair<Double, Double> findDistanceAndAngle() {

        double r;
        double theta;
        r = Math.pow(shootingPositionY, 2) + Math.pow(shootingPositionX, 2);
        r = Math.sqrt(r);
        theta = Math.atan((shootingPositionY / shootingPositionX));
        double newForwards = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);
        if (this.left) {newRight = -1 * newRight;}
        if (this.back) {newForwards = -1 * newForwards;}
        return new Pair<>(newForwards, newRight);
    }

    public void goToShootingPosition(TriFunction<Double, Double, Double, String> driveFunction, HuskyLens huskyLens) {
        DirectionFinder(huskyLens, driveFunction);

        Pair<Double, Double> parameters = findDistanceAndAngle();
        double newForwards = parameters.first;
        double newRight = parameters.second;
        HuskyLens.Block[] blocks = huskyLens.blocks();
        int i = 0;
        while (huskyLensY != shootingPositionY && huskyLensX != shootingPositionX) {
            for (i = 0; i < blocks.length; i++) {
                if (blocks[i] != null) {

                    if (huskyLensIds.contains(blocks[i].id)) {
                        huskyLensX = blocks[i].x;
                        huskyLensY = blocks[i].y;
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
            if (huskyLensY != shootingPositionY && huskyLensX != shootingPositionX) {
                break;
            }
        }
    }
}

