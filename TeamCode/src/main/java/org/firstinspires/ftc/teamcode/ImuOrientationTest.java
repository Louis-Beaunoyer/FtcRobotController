
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.utilities.ImuController;

@TeleOp(name = "TEST - IMU Orientation", group = "Tests")
public class ImuOrientationTest extends OpMode {

    private IMU imu;
    private ImuController imuController;

    @Override
    public void init() {

        imu = hardwareMap.get(IMU.class, Config.IMU_NAME);

        // Use Louis's IMU controller
        imuController = new ImuController(imu);

        // Use the orientation defined in Config.java
        imu.initialize(imuController.getInitializationParameters());

        imuController.resetYaw();

        telemetry.addLine("IMU ready");
        telemetry.update();
    }

    @Override
    public void loop() {

        telemetry.addData("Yaw", "%.2f", imuController.getYawInDegrees());
        telemetry.addData("Pitch", "%.2f", imuController.getPitchInDegrees());
        telemetry.addData("Roll", "%.2f", imuController.getRollInDegrees());

        telemetry.update();
    }
}
