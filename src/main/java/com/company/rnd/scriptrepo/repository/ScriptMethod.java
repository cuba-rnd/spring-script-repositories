package com.company.rnd.scriptrepo.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that should be used to mark interface methods as available for scripting.
 * Also this annotation can be used as meta-annotation for custom annotations for scripted methods, e.g.
 * <code>
 * @ScriptMethod(providerBeanName="sqlScriptProvider", executorBeanName="sqlScriptExecutor")
 * public @interface SqlScriptMathod {...}
 * </code>
 * Then annotation SqlScriptMethod can be used in code.
 *
 * Bean names are used instead of classes for better flexibility.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface ScriptMethod {
    /**
     * Spring bean that returns script text.
     * @return bean name.
     */
    String providerBeanName() default "groovyFileProvider";

    /**
     * Spring bean name that will execute script returned by provider.
     * @return bean name.
     */
    String executorBeanName() default "groovyJsrExecutor";

    /**
     * Optional description for script, can be used for auto-generated documentation of all scripted extensions in the application.
     * @return description string.
     */
    String description() default "";

}
