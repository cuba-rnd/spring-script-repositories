package com.haulmont.scripting.repository.executor;

import java.util.Map;

/**
 * Interface for script executors implementation.
 */
public interface ScriptExecutor {

    /**
     * Executes script. Argument names will be mapped to values one-by-one.
     * @param <T> script return type.
     * @param script Script text.
     * @param parameters Script parameters: name-value pairs
     * @return Script execution result.
     */
    <T> ExecutionResult<T> eval(String script, Map<String, Object> parameters);

}
