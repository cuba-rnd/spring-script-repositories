package com.haulmont.scripting.repository.evaluator;

public class ScriptEvaluationException extends RuntimeException {

    public ScriptEvaluationException(String message) {
        super(message);
    }

    public ScriptEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptEvaluationException(Throwable cause) {
        super(cause);
    }
}
