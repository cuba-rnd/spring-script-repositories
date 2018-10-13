package com.haulmont.scripting.repository.factory;

import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.executor.ScriptExecutor;
import com.haulmont.scripting.repository.provider.ScriptProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Structure for caching method invocation information.
 */
public class ScriptInvocationMetadata {

    private final Method method;
    private final String providerName;
    private final ScriptProvider provider;
    private final String executorName;
    private final ScriptExecutor executor;

    ScriptInvocationMetadata(Method method, String providerName, ScriptProvider provider, String executorName, ScriptExecutor executor) {
        this.method = method;
        this.providerName = providerName;
        this.provider = provider;
        this.executorName = executorName;
        this.executor = executor;
    }

    /**
     * Creates parameters map based on configured parameter names and actual argument values.
     *
     * @param method called method.
     * @param args   actual argument values.
     * @return parameter name - value maps.
     */
    Map<String, Object> createParameterMap(Method method, Object[] args) {
        String[] argNames = Arrays.stream(method.getParameters())
                .map(getParameterName())
                .toArray(String[]::new);
        if (argNames.length != args.length) {
            throw new IllegalArgumentException(String.format("Parameters and args must be the same length. Parameters: %d args: %d", argNames.length, args.length));
        }
        Map<String, Object> paramsMap = new HashMap<>(argNames.length);
        for (int i = 0; i < argNames.length; i++) {
            paramsMap.put(argNames[i], args[i]);
        }
        return paramsMap;
    }

    /**
     * Returns parameter name for a method.
     *
     * @return parameter name.
     */
    private Function<Parameter, String> getParameterName() {
        return p -> p.isAnnotationPresent(ScriptParam.class)
                ? p.getAnnotation(ScriptParam.class).value()
                : p.getName();
    }

    public Method getMethod() {
        return method;
    }

    public String getProviderName() {
        return providerName;
    }

    public ScriptProvider getProvider() {
        return provider;
    }

    public String getExecutorName() {
        return executorName;
    }

    public ScriptExecutor getExecutor() {
        return executor;
    }
}
