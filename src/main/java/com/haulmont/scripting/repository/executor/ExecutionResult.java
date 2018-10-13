package com.haulmont.scripting.repository.executor;

public class ExecutionResult<T> {

    private T value;

    private ExecutionStatus status;

    private Throwable error;

    public ExecutionResult(T value, ExecutionStatus status, Throwable error) {
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
