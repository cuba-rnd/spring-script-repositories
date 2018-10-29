package com.haulmont.scripting.repository.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;


/**
 * Evaluates scripts using JSR-223 javax.script API and bindings.
 */
public abstract class Jsr233Executor implements ScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptJsrExecutor.class);

    @Override
    @SuppressWarnings("unchecked")//Unchecked cast on groovy eval result
    public <T> T eval(String script, Map<String, Object> parameters) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName(getEngineName());
        log.trace("Script bindings: {}", parameters);
        try {
            return (T) scriptEngine.eval(script, new SimpleBindings(parameters));
        } catch (ScriptException e) {
            throw new RuntimeException("Error executing script", e);
        }
    }

    protected abstract String getEngineName();

}
