package com.company.rnd.scriptrepo.repository.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Evaluates Groovy script using JSR-223 javax.script API and bindings.
 */
@Component("groovyJsrExecutor")
public class GroovyScriptJsrExecutor implements ScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptJsrExecutor.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(String script, Method method, String[] argNames, Object[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine groovy = manager.getEngineByName("groovy");
        log.trace("Evaluating script. Method: {}, argNames: {}, args: {}", method.getName(), argNames, args);
        if (argNames.length != args.length) {
            throw new IllegalArgumentException(String.format("Parameters and args must be the same length. Parameters: %d args: %d", argNames.length, args.length));
        }
        Map<String, Object> binds = IntStream.range(0, argNames.length).boxed().
                collect(Collectors.toMap(i -> argNames[i], i -> args[i]));
        log.trace("Bindings: {}",binds);
        try {
            return (T) groovy.eval(script, new SimpleBindings(binds));
        } catch (ScriptException e) {
            throw new RuntimeException("Error executing script", e);
        }
    }
}
