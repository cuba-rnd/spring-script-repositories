package com.haulmont.scripting.core.test.mock;

import com.haulmont.scripting.repository.GroovyScript;
import com.haulmont.scripting.repository.ScriptRepository;

import java.util.Locale;

@ScriptRepository
public interface MockTestScriptRepository {

    @GroovyScript
    String sayHello(Locale locale);

}
