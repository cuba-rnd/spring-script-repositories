package com.haulmont.scripting.core.test.mixed;

import com.haulmont.scripting.repository.ScriptMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ScriptMethod(providerBeanName = "appResourceProvider", executorBeanName = "groovyJsrExecutor")
public @interface AnnotatedGroovyScript {
}
