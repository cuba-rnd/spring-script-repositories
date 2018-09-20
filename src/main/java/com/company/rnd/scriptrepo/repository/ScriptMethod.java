package com.company.rnd.scriptrepo.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScriptMethod {

    String providerBeanName() default "groovyFileProvider";

    String executorBeanName() default "groovyJsrExecutor";

    String description() default "";

}
