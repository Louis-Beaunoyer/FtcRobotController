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

@TeleOp(name = "TEST - Pedro Auto Forward 20cm", group = "Tests")
public class PedroDriveForward20cmTest extends OpMode {

    private static final double TARGET_CM = 20.0;
    private static final double POWER = 0.12;
    private static final double MAX_TIME_SEC = 6.0;
    private static final double CM_PER_INCH = 2.54;

    private TwoWheelLocalizer localizer;
    private Mecanum drivetrain;

    private final ElapsedTime timer = new ElapsedTime();

    private enum State {
        READY, RUNNING, DONE, ABORTED
    }

    private State state = State.READY;
    private String message = "Ready - hold R1 to start";

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

        telemetry.addLine("PEDRO FIRST AUTO TEST");
        telemetry.addLine("Target: 20 cm forward");
        telemetry.addLine("Power: 12%");
        telemetry.addLine("Hold R1 continuously to move");
        telemetry.addLine("Release R1 to stop immediately");
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

        // FIX: Normalize heading to [-180, +180].
        // Example: 359.74 degrees becomes -0.26 degrees.
        double headingDeg = Math.toDegrees(
                Math.atan2(
                        Math.sin(pose.heading()),
                        Math.cos(pose.heading())
                )
        );

        if (state == State.READY && gamepad1.right_bumper) {
            timer.reset();
            state = State.RUNNING;
            message = "Moving forward";
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

            } else if (Math.abs(yCm) > 8.0
                    || Math.abs(headingDeg) > 12.0
                    || xCm < -3.0) {
                abort("Unexpected movement");

            } else if (timer.seconds() > 2.0 && xCm < 2.0) {
                abort("No forward progress");

            } else if (xCm >= TARGET_CM) {
                drivetrain.stop(true);
                state = State.DONE;
                message = "Target reached";

            } else {
                drivetrain.drive(
                        new DrivePowers(POWER, 0, 0),
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

        telemetry.addData("Target X", TARGET_CM);
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
