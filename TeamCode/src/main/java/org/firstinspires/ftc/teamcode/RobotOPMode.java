package org.firstinspires.ftc.teamcode;


import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.dfrobot.HuskyLens;

import org.firstinspires.ftc.teamcode.utilities.AprilTag;
import org.firstinspires.ftc.teamcode.utilities.DriveTrain;
import org.firstinspires.ftc.teamcode.utilities.ImuController;
import org.firstinspires.ftc.teamcode.utilities.SideBallGrabber;


@TeleOp(name = "Main Robot OpMode Dindons", group = "Robot")
public class RobotOPMode extends OpMode{


    Intake intake;
    SideBallGrabber sideBallGrabber;
    DriveTrain driveTrain;

    DcMotorEx intakeMotor;
    DcMotorEx frontLeftDriveTrainMotor;
    DcMotorEx frontRightDriveTrainMotor;
    DcMotorEx backLeftDriveTrainMotor;
    DcMotorEx backRightDriveTrainMotor;
    ImuController imuController;
    IMU imu;
    Config config;
    AprilTag aprilTag;
    HuskyLens huskyLens;

    @Override
    public void init(){
        int soundID = hardwareMap.appContext.getResources().getIdentifier(
                "turkeygobble", // Name of the file without extension (e.g., "quack")
                "raw",
                hardwareMap.appContext.getPackageName()
                );
        // 1. Create a sound parameters object
        SoundPlayer.PlaySoundParams params = new SoundPlayer.PlaySoundParams();

// 2. Adjust the volume scale (0.0f to 1.0f)
        params.volume = 0.99f; // Sets the volume to 50%

// 3. Configure standard behaviors (Optional)
        params.loopControl = 0; // 0 = play once, -1 = loop indefinitely
        params.waitForNonLoopingSoundsToFinish = true;

// 4. Play the sound using the overloaded method that accepts your params object
        SoundPlayer.getInstance().startPlaying(hardwareMap.appContext, soundID, params, null, null);





        intakeMotor = hardwareMap.get(DcMotorEx.class, config.INTAKE_MOTOR_NAME);
        CRServo leftSideBallGrabberServo = hardwareMap.get(CRServo.class, config.SIDE_BALL_GRABBER_LEFT_MOTOR_NAME);
        CRServo rightSideBallGrabberServo = hardwareMap.get(CRServo.class, config.SIDE_BALL_GRABBER_RIGHT_MOTOR_NAME);
        frontLeftDriveTrainMotor = hardwareMap.get(DcMotorEx.class, config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);
        frontRightDriveTrainMotor = hardwareMap.get(DcMotorEx.class, config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);
        backLeftDriveTrainMotor = hardwareMap.get(DcMotorEx.class, config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);
        backRightDriveTrainMotor = hardwareMap.get(DcMotorEx.class, config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);
        huskyLens = hardwareMap.get(HuskyLens.class, config.CAMERA_NAME);
        imu = hardwareMap.get(IMU.class, "imu");
        imuController = new ImuController(imu);
        config = new Config();
        config.init(false);
        imuController.init();



        sideBallGrabber = new SideBallGrabber(leftSideBallGrabberServo, rightSideBallGrabberServo);
        driveTrain = new DriveTrain(frontLeftDriveTrainMotor,frontRightDriveTrainMotor,backLeftDriveTrainMotor,backRightDriveTrainMotor, imuController);
        intake = new Intake(intakeMotor, gamepad1, gamepad2);
        driveTrain.init();
        sideBallGrabber.init();
        intake.init();
        aprilTag = new AprilTag(huskyLens, driveTrain);
        aprilTag.init();









    }

    public void loop(){
        intake.RunIntake();
        sideBallGrabber.setGrabberSpeed(gamepad1.left_trigger);
        sideBallGrabber.loop();

        if (gamepad1.square) {
            imuController.resetYaw();
            telemetry.addData("Reset Yaw", "Reseting Yaw");
        }
        telemetry.addData("Yaw", imuController.getYawInDegrees());
        aprilTag.loop();

        if (gamepad1.cross && !aprilTag.isAligning()) {
            aprilTag.startsAligningToClosestTag();
        }
        if (Math.abs(gamepad1.left_stick_y) > 0.1|| Math.abs(gamepad1.left_stick_x) > 0.1 || Math.abs(gamepad1.right_stick_x) > 0.1) {
            aprilTag.stopAligning();
        }
        if (aprilTag.isAligning()) {
            aprilTag.followIdToShootingPosition();
        }else {
            driveTrain.driveFieldRelative(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        }

        if (gamepad1.triangle){ driveTrain.turnToClosest180();}

        aprilTag.telemetry(telemetry);
        telemetry.update();





    }


}