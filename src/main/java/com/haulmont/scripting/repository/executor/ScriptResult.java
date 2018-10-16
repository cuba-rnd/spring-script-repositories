package com.haulmont.scripting.repository.executor;

public class ScriptResult<T> {

    private T value;

    private ExecutionStatus status;

    private Throwable error;

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
