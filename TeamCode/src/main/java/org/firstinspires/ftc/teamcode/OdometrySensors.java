
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class OdometrySensors {

    // Two odopod encoders used to measure robot movement
    private final DcMotor parallelEncoder;
    private final DcMotor perpendicularEncoder;

    // IMU used to measure the robot rotation
    private final IMU imu;

    // Adjust encoder signs according to the real mounting
    private final int parallelDirection;
    private final int perpendicularDirection;

    // Connect the odometry sensors using the robot hardware map
    // Hardware names and encoder directions are provided by Robot.java
    public OdometrySensors(HardwareMap hardwareMap,
                           String parallelName,
                           String perpendicularName,
                           String imuName,
                           boolean reverseParallel,
                           boolean reversePerpendicular) {

        parallelEncoder =
                hardwareMap.get(DcMotor.class, parallelName);

        perpendicularEncoder =
                hardwareMap.get(DcMotor.class, perpendicularName);

        imu = hardwareMap.get(IMU.class, imuName);

        parallelDirection = reverseParallel ? -1 : 1;
        perpendicularDirection = reversePerpendicular ? -1 : 1;
    }

    // Reads the encoder measuring forward and backward movement
    public int getParallelTicks() {
        return parallelEncoder.getCurrentPosition()
                * parallelDirection;
    }

    // Reads the encoder measuring left and right movement
    public int getPerpendicularTicks() {
        return perpendicularEncoder.getCurrentPosition()
                * perpendicularDirection;
    }

    // Reads the robot heading in radians
    // IMU initialization and reset are controlled by Robot.java
    public double getHeadingRadians() {
        return imu.getRobotYawPitchRollAngles()
                .getYaw(AngleUnit.RADIANS);
    }

    // Sets the current readings as the odometry starting point
    // This does not reset the physical encoders or the IMU
    public void resetOdometry(Odometry odometry) {
        odometry.reset(
                getParallelTicks(),
                getPerpendicularTicks(),
                getHeadingRadians()
        );
    }

    // Reads the sensors and updates the calculated robot position
    // Robot.java should call this once per update cycle normaly
    public void updateOdometry(Odometry odometry) {
        odometry.update(
                getParallelTicks(),
                getPerpendicularTicks(),
                getHeadingRadians()
        );
    }
}
