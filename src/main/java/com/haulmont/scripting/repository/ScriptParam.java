package com.haulmont.scripting.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  We need it to get real argument names in runtime if jar was compiled without debug information.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ScriptParam {

    /**
     * Parameter name that will be used in script.
     * @return parameter name.
     */
    String value();

}
