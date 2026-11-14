package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;

import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;

import org.firstinspires.ftc.teamcode.utilities.PedroLocalizationConfig;
import org.firstinspires.ftc.teamcode.utilities.PedroPathing;

public final class Tuning {

    private Tuning() {
    }

    @Tuner
    public static Procedure foresightTuner() {

        return new ForesightTuner(

                hardwareMap -> new TwoWheelLocalizer(
                        hardwareMap,
                        PedroLocalizationConfig.create()
                ),

                hardwareMap -> new Mecanum(
                        hardwareMap,
                        PedroPathing.createMecanumConfig()
                )
        );
    }
}
