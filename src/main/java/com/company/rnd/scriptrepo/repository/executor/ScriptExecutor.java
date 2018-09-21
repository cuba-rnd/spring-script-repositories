package com.company.rnd.scriptrepo.repository.executor;

import java.lang.reflect.Method;

public interface ScriptExecutor {

    <T> T eval(String script, Method method, String[] argNames, Object[] args);

}
