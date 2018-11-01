package com.haulmont.scripting.repository.provider;

public class ScriptNotFoundException extends RuntimeException {

    public ScriptNotFoundException() {
    }

    public ScriptNotFoundException(String message) {
        super(message);
    }

    public ScriptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptNotFoundException(Throwable cause) {
        super(cause);
    }
}
