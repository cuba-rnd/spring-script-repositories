package com.haulmont.scripting.repository.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Provider for groovy scripts stored in application resources.
 */
@Component("javaScriptResourceProvider")
public class JavaScriptResourceProvider extends AppResourceProvider {

    @Value("${js.script.source.root.path}")
    private String rootPath;

    /**
     * Generates script location path based on rule: rootPath/InterfaceName.methodName.js.
     * @param method scripted method.
     * @return resource path string.
     */
    @Override
    public String getResourcePath(Method method) {
        return rootPath + "/" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + ".js";
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
