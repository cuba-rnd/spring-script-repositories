package com.haulmont.scripting.repository.evaluator;

public enum EvaluationStatus {
    SUCCESS(true),
    FAILURE(false);

    private final boolean successful;

    public boolean isSuccessful() {
        return successful;
    }

    EvaluationStatus(boolean successful) {
        this.successful = successful;
    }
}
