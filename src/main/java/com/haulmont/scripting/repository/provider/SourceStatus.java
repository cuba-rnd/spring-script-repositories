package com.haulmont.scripting.repository.provider;

public enum SourceStatus {
    FOUND (true),
    NOT_FOUND (false),
    FAILURE (false);

    private final boolean successful;

    public boolean isSuccessful() {
        return successful;
    }

    SourceStatus(boolean successful) {
        this.successful = successful;
    }
}
