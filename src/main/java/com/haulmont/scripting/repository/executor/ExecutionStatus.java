package com.haulmont.scripting.repository.executor;

public enum ExecutionStatus {
    SUCCESS(true),
    FAILURE(false);

    private final boolean successful;

    public boolean isSuccessful() {
        return successful;
    }

    ExecutionStatus(boolean successful) {
        this.successful = successful;
    }
}
