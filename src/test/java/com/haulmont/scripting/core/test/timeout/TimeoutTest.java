package com.haulmont.scripting.core.test.timeout;

import com.haulmont.scripting.repository.evaluator.EvaluationStatus;
import com.haulmont.scripting.repository.evaluator.ScriptEvaluationException;
import com.haulmont.scripting.repository.evaluator.ScriptResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ContextConfiguration(locations = {"classpath:com/haulmont/scripting/core/test/timeout/timeout-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeoutTest {

    @Autowired
    private TimeoutTestScriptRepository testScriptRepository;

    @Test//Timeout 1_000L
    public void runShortJob() {
        String result = testScriptRepository.doLongJob(100L);
        assertEquals(testScriptRepository.SUCCESS, result);
    }

    @Test(expected = ScriptEvaluationException.class) //Timeout 1_000L
    public void runLongJob() {
        testScriptRepository.doLongJob(10_000L);
        fail("Long-running methods must throw exception if timeout is set");
    }


    @Test//Timeout 100L
    public void runLongJobComposedTimeout() {
        ScriptResult<String> result = testScriptRepository.doAnotherLongJob(10_000L);
        assertEquals(EvaluationStatus.FAILURE, result.getStatus());
        assertEquals(ScriptEvaluationException.class, result.getError().getClass());
    }


    @Test(expected = ScriptEvaluationException.class) //Timeout 1_000L
    public void runLongMethodComposedWithTimeout() {
        try {
            Executors.newSingleThreadExecutor().submit(() -> testScriptRepository.doThirdLongJob(5_000L));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Executors.newSingleThreadExecutor().submit(() -> testScriptRepository.doThirdLongJob(5_000L));
        } catch (Exception e) {
            e.printStackTrace();
        }
        testScriptRepository.doThirdLongJob(5_000L);
        fail("Long-running methods must throw exception if timeout is set");
    }


}
