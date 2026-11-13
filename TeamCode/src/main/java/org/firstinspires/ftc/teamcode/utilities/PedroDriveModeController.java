package org.firstinspires.ftc.teamcode.utilities;

public final class PedroDriveModeController {

    public enum Mode {
        LOCKED,
        MANUAL,
        AUTO,
        FAULT
    }

    private final PedroMotorSafety motorSafety;
    private final PedroPathing pedro;

    private Mode mode = Mode.LOCKED;
    private String faultReason = "";

    public PedroDriveModeController(
            PedroMotorSafety motorSafety,
            PedroPathing pedro) {

        if (motorSafety == null) {
            throw new IllegalArgumentException(
                    "Motor safety is required");
        }

        this.motorSafety = motorSafety;

        // Pedro may be null until Foresight is ready.
        this.pedro = pedro;
    }

    // Request manual control.
    public boolean enableManual() {
        return changeMode(Mode.MANUAL);
    }

    // Request autonomous control only when ready.
    public boolean enableAuto(boolean configurationReady) {

        if (!configurationReady || pedro == null) {
            return false;
        }

        return changeMode(Mode.AUTO);
    }

    // Lock propulsion and request a direct motor stop.
    public boolean lock() {
        return changeMode(Mode.LOCKED);
    }

    private boolean changeMode(Mode requestedMode) {

        if (mode == Mode.FAULT
                || motorSafety.isFaultLatched()) {
            return false;
        }

        // Cancel Pedro before leaving autonomous mode.
        if (mode == Mode.AUTO && pedro != null) {

            try {
                pedro.stop();

            } catch (RuntimeException e) {

                emergencyStop(
                        "Pedro cancellation failed");
                return false;
            }
        }

        // Stop the physical motors before changing mode.
        if (!motorSafety.stopAll()) {

            emergencyStop(
                    "Direct motor stop failed");
            return false;
        }

        mode = requestedMode;
        return true;
    }

    // Latch the fault and attempt to stop everything.
    public void emergencyStop(String reason) {

        mode = Mode.FAULT;

        faultReason =
                reason == null || reason.isEmpty()
                        ? "Emergency stop"
                        : reason;

        // Try to cancel Pedro, but always attempt
        // the direct motor stop even if cancellation fails.
        if (pedro != null) {

            try {
                pedro.stop();

            } catch (RuntimeException ignored) {
                // Continue to direct motor stop.
            }
        }

        motorSafety.emergencyStop(faultReason);
    }

    public boolean canDriveManually() {
        return mode == Mode.MANUAL
                && !motorSafety.isFaultLatched();
    }

    public boolean canRunPedro() {
        return mode == Mode.AUTO
                && pedro != null
                && !motorSafety.isFaultLatched();
    }

    public boolean hasFault() {
        return mode == Mode.FAULT
                || motorSafety.isFaultLatched();
    }

    public Mode getMode() {
        return mode;
    }

    public String getFaultReason() {

        if (motorSafety.isFaultLatched()) {
            return motorSafety.getFaultReason();
        }

        return faultReason;
    }
}
