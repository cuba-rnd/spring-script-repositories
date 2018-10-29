package com.haulmont.scripting.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ScriptMethod(providerBeanName = "javaScriptResourceProvider", executorBeanName = "javaScriptJsrExecutor")
public @interface JsMethod {
}
