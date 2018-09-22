package com.company.rnd.scriptrepo.repository.config;

import java.lang.annotation.Annotation;

/**
 * Struct like class to hold scripted method data.
 */
public class ScriptInfo {
    public final Class<? extends Annotation> scriptAnnotation;
    public final String provider;
    public final String executor;

    public ScriptInfo(Class<? extends Annotation> scriptAnnotation, String provider, String executor) {
        this.scriptAnnotation = scriptAnnotation;
        this.provider = provider;
        this.executor = executor;
    }

    @Override
    public String toString() {
        return "ScriptInfo{" +
                "scriptAnnotation=" + scriptAnnotation.getName() +
                ", provider='" + provider + '\'' +
                ", executor='" + executor + '\'' +
                '}';
    }
}