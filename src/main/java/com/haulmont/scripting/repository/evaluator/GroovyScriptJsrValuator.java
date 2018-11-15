package com.haulmont.scripting.repository.evaluator;

import org.springframework.stereotype.Component;

/**
 * Evaluates Groovy script using JSR-223 javax.script API and bindings.
 */
@Component("groovyJsrEvaluator")
public class GroovyScriptJsrValuator extends Jsr233Evaluator {

    protected String getEngineName() {
        return "groovy";
    }
}
