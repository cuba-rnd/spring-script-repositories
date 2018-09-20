package com.company.rnd.scriptrepo.repository.factory;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ScriptRepositoryNamespaceHandler extends NamespaceHandlerSupport {

    public static final String SCRIPT_REPOSITORIES_TAG_NAME  = "script-repositories";

    @Override
    public void init() {
        registerBeanDefinitionParser(SCRIPT_REPOSITORIES_TAG_NAME, new ScriptRepositoryConfigurationParser());
    }
}
