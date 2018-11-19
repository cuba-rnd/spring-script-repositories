package com.haulmont.scripting.repository.config;

import com.haulmont.scripting.repository.ScriptParam;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Struct like class to hold scripted method data taken from annotation.
 */
@SuppressWarnings("serial")
public class AnnotationConfig implements Serializable {

    public final Class<? extends Annotation> scriptAnnotation;
    public final String provider;
    public final String evaluator;
    public final long timeout;
    public final String description;

    public AnnotationConfig(Class<? extends Annotation> scriptAnnotation, String provider, String evaluator, long timeout, String description) {
        this.scriptAnnotation = scriptAnnotation;
        this.provider = provider;
        this.evaluator = evaluator;
        this.timeout = timeout;
        this.description = description;
    }

    @Override
    public String toString() {
        return "AnnotationConfig{" +
                "scriptAnnotation=" + scriptAnnotation +
                ", provider='" + provider + '\'' +
                ", evaluator='" + evaluator + '\'' +
                ", timeout=" + timeout +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Creates parameters map based on configured parameter names and actual argument values.
     *
     * @param method called method.
     * @param args   actual argument values.
     * @return parameter name - value maps.
     */
    public Map<String, Object> createParameterMap(Method method, Object[] args) {
        String[] argNames = Arrays.stream(method.getParameters())
                .map(getParameterName())
                .toArray(String[]::new);
        int length = args != null ? args.length : 0;
        if (argNames.length != length) {
            throw new IllegalArgumentException(String.format("Parameters and args must be the same length. Parameters: %d args: %d", argNames.length, length));
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


}