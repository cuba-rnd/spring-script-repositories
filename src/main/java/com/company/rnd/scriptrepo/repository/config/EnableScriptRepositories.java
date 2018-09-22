package com.company.rnd.scriptrepo.repository.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable scripting repositories.
 * Will scan packages of the annotated configuration class for scripting repositories.
 *
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ScriptRepositoriesRegistrar.class)
public @interface EnableScriptRepositories {
    /**
     * Packages to be scanned.
     * @return aray of package names.
     */
    String[] basePackages();

}
