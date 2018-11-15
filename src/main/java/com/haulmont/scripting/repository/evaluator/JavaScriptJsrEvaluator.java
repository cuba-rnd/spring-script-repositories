package com.haulmont.scripting.repository.evaluator;

import org.springframework.stereotype.Component;

/**
 * Evaluates Groovy script using JSR-223 javax.script API and bindings.
 */
@Component("javaScriptJsrEvaluator")
public class JavaScriptJsrEvaluator extends Jsr233Evaluator {

    protected String getEngineName() {
        return "javascript";
    }
}
