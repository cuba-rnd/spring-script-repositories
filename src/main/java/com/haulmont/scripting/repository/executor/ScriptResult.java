package com.haulmont.scripting.repository.executor;

/**
 * Wrapper for script execution result. Contains result object (null in case of execution error),
 * execution status and execution error (if any).
 *
 * @param <T> script execution result type.
 */
public class ScriptResult<T> {

    private final T value;

    private final ExecutionStatus status;

    private final Throwable error;

    public ScriptResult(T value, ExecutionStatus status, Throwable error) {
        this.value = value;
        this.status = status;
        this.error = error;
    }

    public T getValue() {
        return value;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Throwable getError() {
        return error;
    }
}
