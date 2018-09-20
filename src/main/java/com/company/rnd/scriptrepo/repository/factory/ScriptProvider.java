package com.company.rnd.scriptrepo.repository.factory;

import java.lang.reflect.Method;

/**
 * Generic interface, use it to implement beans that provide scripts for execution.
 */
public interface ScriptProvider {

    /**
     * Gets script text based on method signature.
     * Please note that you may want to implement <code>com.haulmont.cuba.security.app.Authenticated</code> from
     * core module to get scripts protected by row-level security.
     * @param method Script Repository interface method to be executed.
     * @return Script text associated with this method.
     */
    String getScript(Method method);

}
