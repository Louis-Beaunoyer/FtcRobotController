package org.firstinspires.ftc.teamcode;


import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

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





        intakeMotor = hardwareMap.get(DcMotorEx.class, Config.INTAKE_MOTOR_NAME);
        CRServo leftSideBallGrabberServo = hardwareMap.get(CRServo.class, Config.SIDE_BALL_GRABBER_LEFT_MOTOR_NAME);
        CRServo rightSideBallGrabberServo = hardwareMap.get(CRServo.class, Config.SIDE_BALL_GRABBER_RIGHT_MOTOR_NAME);
        imu = hardwareMap.get(IMU.class, "imu");
        imuController = new ImuController(imu);
        intake = new Intake(intakeMotor, gamepad1, gamepad2);
        config = new Config(false);
        sideBallGrabber = new SideBallGrabber(leftSideBallGrabberServo, rightSideBallGrabberServo);
        driveTrain = new DriveTrain(frontLeftDriveTrainMotor,frontRightDriveTrainMotor,backLeftDriveTrainMotor,backRightDriveTrainMotor, imuController, config);
        frontLeftDriveTrainMotor = hardwareMap.get(DcMotorEx.class, Config.FRONT_LEFT_DRIVE_TRAIN_MOTOR_NAME);
        frontRightDriveTrainMotor = hardwareMap.get(DcMotorEx.class, Config.FRONT_RIGHT_DRIVE_TRAIN_MOTOR_NAME);
        backLeftDriveTrainMotor = hardwareMap.get(DcMotorEx.class, Config.BACK_LEFT_DRIVE_TRAIN_MOTOR_NAME);
        backRightDriveTrainMotor = hardwareMap.get(DcMotorEx.class, Config.BACK_RIGHT_DRIVE_TRAIN_MOTOR_NAME);







    }

    public void loop(){
        intake.RunIntake();
        sideBallGrabber.setGrabberSpeed(1);
        if (gamepad1.a){
            sideBallGrabber.toogleGrabber(false);
        }
        driveTrain.driveFieldRelative(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

    }


}