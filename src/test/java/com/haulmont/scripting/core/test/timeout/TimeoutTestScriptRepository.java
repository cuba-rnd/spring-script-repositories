package com.haulmont.scripting.core.test.timeout;

import com.haulmont.scripting.repository.GroovyScript;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

@ScriptRepository
public interface TimeoutTestScriptRepository {

    String SUCCESS = "Success!";

    @GroovyScript (timeout = 1_000L)
    default String doLongJob(Long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
            return SUCCESS;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @TestComposedTimeout
    default String doAnotherLongJob(Long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
            return SUCCESS;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @TestComposedTimeout (timeout = 1_000L)
    void doThirdLongJob(@ScriptParam("timeMillis") Long timeInMillis);

}
