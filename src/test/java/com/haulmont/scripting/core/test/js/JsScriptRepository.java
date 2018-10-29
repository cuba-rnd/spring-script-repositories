package com.haulmont.scripting.core.test.js;

import com.haulmont.scripting.repository.JsMethod;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

@ScriptRepository
public interface JsScriptRepository {

    @JsMethod
    int simpleMath(@ScriptParam("x") int x, @ScriptParam("y") int y);

}
