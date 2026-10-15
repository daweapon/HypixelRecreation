package dev.daweapon.service.orchestrator.instance;

public enum FailureReason {
    NO_SERVER(true),
    FULL(true),
    TIMEOUT(false),
    REFUSED(false);

    private final boolean worthWaiting;

    FailureReason(boolean worthWaiting) {
        this.worthWaiting = worthWaiting;
    }

    public boolean worthWaiting() {
        return worthWaiting;
    }
}
