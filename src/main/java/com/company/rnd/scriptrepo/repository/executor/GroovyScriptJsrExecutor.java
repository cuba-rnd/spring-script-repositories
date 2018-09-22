package com.company.rnd.scriptrepo.repository.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * Evaluates Groovy script using JSR-223 javax.script API and bindings.
 */
@Component("groovyJsrExecutor")
public class GroovyScriptJsrExecutor implements ScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptJsrExecutor.class);

    @Override
    @SuppressWarnings("unchecked")//Unchecked cast on groovy eval result
    public <T> T eval(String script, Map<String, Object> parameters) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine groovy = manager.getEngineByName("groovy");
        log.trace("Script bindings: {}", parameters);
        try {
            return (T) groovy.eval(script, new SimpleBindings(parameters));
        } catch (ScriptException e) {
            throw new RuntimeException("Error executing script", e);
        }
    }
}
