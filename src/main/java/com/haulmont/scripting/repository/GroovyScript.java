package com.haulmont.scripting.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Alias for @{@link ScriptMethod} default annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ScriptMethod
public @interface GroovyScript {

    long timeout() default -1;

}
