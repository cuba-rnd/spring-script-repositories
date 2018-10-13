package com.haulmont.scripting.repository.config;

import java.lang.annotation.Annotation;

/**
 * Struct like class to hold scripted method data.
 */
public class AnnotationConfig {
    public final Class<? extends Annotation> scriptAnnotation;
    public final String provider;
    public final String executor;

    public AnnotationConfig(Class<? extends Annotation> scriptAnnotation, String provider, String executor) {
        this.scriptAnnotation = scriptAnnotation;
        this.provider = provider;
        this.executor = executor;
    }

    @Override
    public String toString() {
        return "AnnotationConfig{" +
                "scriptAnnotation=" + scriptAnnotation.getName() +
                ", provider='" + provider + '\'' +
                ", executor='" + executor + '\'' +
                '}';
    }
}