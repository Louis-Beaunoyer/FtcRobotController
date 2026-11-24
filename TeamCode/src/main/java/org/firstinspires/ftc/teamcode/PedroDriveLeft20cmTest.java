package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.utilities.PedroLocalizationConfig;
import org.firstinspires.ftc.teamcode.utilities.PedroPathing;

@TeleOp(name = "TEST - Pedro Auto Left 20cm", group = "Tests")
public class PedroDriveLeft20cmTest extends OpMode {

    private static final double TARGET_CM = 20.0;
    private static final double POWER = 0.12;
    private static final double MAX_TIME_SEC = 8.0;
    private static final double CM_PER_INCH = 2.54;

    private TwoWheelLocalizer localizer;
    private Mecanum drivetrain;

    private final ElapsedTime timer = new ElapsedTime();

    private enum State {
        READY, RUNNING, DONE, ABORTED
    }

    private State state = State.READY;
    private String message = "Ready - hold R1";

    @Override
    public void init() {

        localizer = new TwoWheelLocalizer(
                hardwareMap,
                PedroLocalizationConfig.create()
        );

        drivetrain = new Mecanum(
                hardwareMap,
                PedroPathing.createMecanumConfig()
        );

        drivetrain.stop(true);

        telemetry.addLine("PEDRO AUTO LEFT TEST");
        telemetry.addLine("Target: 20 cm LEFT");
        telemetry.addLine("Power: 12%");
        telemetry.addLine("Hold R1 to move");
        telemetry.addLine("Release R1 to stop");
        telemetry.update();
    }

    @Override
    public void start() {

        drivetrain.stop(true);
        localizer.setPose(Pose.zero());

        state = State.READY;
        message = "Hold R1 to start";
    }

    @Override
    public void loop() {

        localizer.update();

        Pose pose = localizer.pose();

        double xCm = pose.x() * CM_PER_INCH;
        double yCm = pose.y() * CM_PER_INCH;

        // Normalize angle between -180 and +180 degrees.
        double headingDeg = Math.toDegrees(
                Math.atan2(
                        Math.sin(pose.heading()),
                        Math.cos(pose.heading())
                )
        );

        if (state == State.READY && gamepad1.right_bumper) {
            timer.reset();
            state = State.RUNNING;
            message = "Moving LEFT";
        }

        if (state == State.RUNNING) {

            if (!gamepad1.right_bumper) {
                abort("R1 released");

            } else if (!Double.isFinite(xCm)
                    || !Double.isFinite(yCm)
                    || !Double.isFinite(headingDeg)) {
                abort("Invalid localization");

            } else if (timer.seconds() > MAX_TIME_SEC) {
                abort("Timeout");

            } else if (Math.abs(xCm) > 8.0
                    || Math.abs(headingDeg) > 12.0
                    || yCm < -3.0) {
                abort("Unexpected movement");

            } else if (timer.seconds() > 2.0 && yCm < 2.0) {
                abort("No lateral progress");

            } else if (yCm >= TARGET_CM) {

                drivetrain.stop(true);
                state = State.DONE;
                message = "Target reached";

            } else {

                // Positive strafe = LEFT in Pedro.
                drivetrain.drive(
                        new DrivePowers(0, POWER, 0),
                        true
                );
            }

        } else {
            drivetrain.stop(true);
        }

        telemetry.addData("Status", state);
        telemetry.addData("Message", message);

        telemetry.addData("Pedro X (cm)", "%.2f", xCm);
        telemetry.addData("Pedro Y (cm)", "%.2f", yCm);
        telemetry.addData("Heading (deg)", "%.2f", headingDeg);

        telemetry.addData("Target Y (cm)", TARGET_CM);
        telemetry.addData("Power", POWER);
        telemetry.addData("Time (s)", "%.2f", timer.seconds());

        telemetry.addLine("Hold R1 while moving");
        telemetry.update();
    }

    private void abort(String reason) {

        drivetrain.stop(true);
        state = State.ABORTED;
        message = reason;
    }

    @Override
    public void stop() {

        if (drivetrain != null) {
            drivetrain.stop(true);
        }
    }
}
