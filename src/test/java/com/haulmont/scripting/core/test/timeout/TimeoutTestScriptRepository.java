package com.haulmont.scripting.core.test.timeout;

import com.haulmont.scripting.repository.GroovyScript;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.evaluator.EvaluationStatus;
import com.haulmont.scripting.repository.evaluator.ScriptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScriptRepository
public interface TimeoutTestScriptRepository {

    Logger log = LoggerFactory.getLogger(TimeoutTestScriptRepository.class);


    String SUCCESS = "Success!";

    @GroovyScript (timeout = 1_000L)
    default String doLongJob(Long timeInMillis) {
        try {
            log.info("Will sleep for {} ms", timeInMillis);
            Thread.sleep(timeInMillis);
            return SUCCESS;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @TestComposedTimeout
    default ScriptResult<String> doAnotherLongJob(Long timeInMillis) {
        try {
            log.info("Will sleep again for {} ms", timeInMillis);
            Thread.sleep(timeInMillis);
            return new ScriptResult<>(SUCCESS, EvaluationStatus.SUCCESS, null);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @TestComposedTimeout (timeout = 1_000L)
    void doThirdLongJob(@ScriptParam("timeMillis") Long timeInMillis);

}
