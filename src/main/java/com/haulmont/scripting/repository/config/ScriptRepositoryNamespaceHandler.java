package com.haulmont.scripting.repository.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers parser for XML based configuration for script repositories.
 */
public class ScriptRepositoryNamespaceHandler extends NamespaceHandlerSupport {

    public static final String SCRIPT_REPOSITORIES_TAG_NAME  = "script-repositories";

    /**
     * @see NamespaceHandlerSupport#init()
     */
    @Override
    public void init() {
        registerBeanDefinitionParser(SCRIPT_REPOSITORIES_TAG_NAME, new ScriptRepositoryConfigurationParser());
    }
}
