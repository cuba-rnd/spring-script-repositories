package com.haulmont.scripting.repository.config;

import java.lang.annotation.Annotation;

/**
 * Struct like class to hold scripted method data.
 */
public class AnnotationConfig {
    public final Class<? extends Annotation> scriptAnnotation;
    public final String provider;
    public final String executor;
    public final String description;

    public AnnotationConfig(Class<? extends Annotation> scriptAnnotation, String provider, String executor, String description) {
        this.scriptAnnotation = scriptAnnotation;
        this.provider = provider;
        this.executor = executor;
        this.description = description;
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