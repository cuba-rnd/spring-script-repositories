package com.haulmont.scripting.core.test.js;

import com.haulmont.scripting.repository.JavaScript;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

@ScriptRepository
public interface JsScriptRepository {

    @JavaScript
    double simpleMath(@ScriptParam("x") double x, @ScriptParam("y") double y);

}
