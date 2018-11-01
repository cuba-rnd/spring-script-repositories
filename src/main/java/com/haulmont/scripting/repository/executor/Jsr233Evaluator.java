package com.haulmont.scripting.repository.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.ScriptSource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;


/**
 * Evaluates scripts using JSR-223 javax.script API and bindings.
 */
public abstract class Jsr233Evaluator implements ScriptEvaluator {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptJsrValuator.class);


    @Override
    public Object evaluate(ScriptSource script) throws ScriptCompilationException {
        return eval(script, Collections.emptyMap());
    }

    @Override
    public Object evaluate(ScriptSource script, Map<String, Object> arguments) throws ScriptCompilationException {
        return eval(script, arguments);
    }

    private Object eval(ScriptSource script, Map<String, Object> parameters) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName(getEngineName());
        log.trace("Script bindings: {}", parameters);
        try {
            return scriptEngine.eval(script.getScriptAsString(), new SimpleBindings(parameters));
        } catch (IOException | ScriptException e) {
            throw new ScriptCompilationException("Error executing script", e);
        }
    }

    protected abstract String getEngineName();

}
