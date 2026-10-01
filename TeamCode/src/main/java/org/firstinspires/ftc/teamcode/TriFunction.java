package org.firstinspires.ftc.teamcode;

@FunctionalInterface
public interface TriFunction<Forwards, Right, Rotate, R> {
    R apply(Forwards forwards, Right right, Rotate rotate);
}
