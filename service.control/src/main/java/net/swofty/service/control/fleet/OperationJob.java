package net.swofty.service.control.fleet;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A long-running fleet operation.
 *
 * Staggered starts take minutes, far longer than a sensible HTTP timeout, so the control plane
 * returns a job id immediately and the client polls this for progress.
 */
public final class OperationJob {

    public enum Status {
        RUNNING,
        SUCCEEDED,
        /** Finished, but at least one step failed. The sequence still ran to completion. */
        COMPLETED_WITH_ERRORS,
        FAILED
    }

    public record Step(Instant at, String container, String action, boolean ok, String message) {
    }

    private final String id;
    private final String operation;
    private final Instant startedAt = Instant.now();
    private final List<Step> steps = new CopyOnWriteArrayList<>();
    private final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNING);
    private final AtomicReference<String> currentStep = new AtomicReference<>("starting");
    private volatile Instant finishedAt;
    private final String requestedBy;

    public OperationJob(String id, String operation, String requestedBy) {
        this.id = id;
        this.operation = operation;
        this.requestedBy = requestedBy;
    }

    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Status getStatus() {
        return status.get();
    }

    public String getCurrentStep() {
        return currentStep.get();
    }

    public List<Step> getSteps() {
        return List.copyOf(steps);
    }

    public void setCurrentStep(String step) {
        currentStep.set(step);
    }

    public void record(String container, String action, boolean ok, String message) {
        steps.add(new Step(Instant.now(), container, action, ok, message));
    }

    public void finish() {
        boolean anyFailed = steps.stream().anyMatch(step -> !step.ok());
        status.set(anyFailed ? Status.COMPLETED_WITH_ERRORS : Status.SUCCEEDED);
        currentStep.set("done");
        finishedAt = Instant.now();
    }

    public void fail(String message) {
        status.set(Status.FAILED);
        currentStep.set(message);
        finishedAt = Instant.now();
    }

    public boolean isTerminal() {
        return status.get() != Status.RUNNING;
    }
}
