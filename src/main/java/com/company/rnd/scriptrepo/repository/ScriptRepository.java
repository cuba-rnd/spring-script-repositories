package com.company.rnd.scriptrepo.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for script repository interface.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptRepository {

    /**
     * Optional description for script repository. E.g. "Customer manipulation-related scripts".
     * @return script repository description.
     */
    String description() default "";

}
