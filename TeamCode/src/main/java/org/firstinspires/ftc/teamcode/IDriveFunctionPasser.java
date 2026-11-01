package org.firstinspires.ftc.teamcode;

@FunctionalInterface
public interface IDriveFunctionPasser<Forwards, Right, Rotate, R> {
    R apply(Forwards forwards, Right right, Rotate rotate);
}
