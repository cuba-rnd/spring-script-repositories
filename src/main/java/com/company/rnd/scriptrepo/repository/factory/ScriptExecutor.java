package com.company.rnd.scriptrepo.repository.factory;

import java.lang.reflect.Method;

public interface ScriptExecutor {

    <T> T eval(String script, Method method, String[] argNames, Object[] args);

}
