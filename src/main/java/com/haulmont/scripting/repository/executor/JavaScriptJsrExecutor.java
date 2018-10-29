package com.haulmont.scripting.repository.executor;

import org.springframework.stereotype.Component;

/**
 * Evaluates Groovy script using JSR-223 javax.script API and bindings.
 */
@Component("javaScriptJsrExecutor")
public class JavaScriptJsrExecutor extends Jsr233Executor {

    protected String getEngineName() {
        return "javascript";
    }
}
