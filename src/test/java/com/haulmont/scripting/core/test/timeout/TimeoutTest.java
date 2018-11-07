package com.haulmont.scripting.core.test.timeout;

import com.haulmont.scripting.repository.executor.ExecutionStatus;
import com.haulmont.scripting.repository.executor.ScriptExecutionException;
import com.haulmont.scripting.repository.executor.ScriptResult;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Locale;
import java.util.concurrent.TimeoutException;

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

    @Test(expected = ScriptExecutionException.class) //Timeout 1_000L
    public void runLongJob() {
        testScriptRepository.doLongJob(10_000L);
        fail("Long-running methods must throw exception if timeout is set");
    }


    @Test//Timeout 100L
    public void runLongJobComposedTimeout() {
        ScriptResult<String> result = testScriptRepository.doAnotherLongJob(10_000L);
        assertEquals(ExecutionStatus.FAILURE, result.getStatus());
        assertEquals(ScriptExecutionException.class, result.getError().getClass());
    }


    @Test(expected = ScriptExecutionException.class) //Timeout 1_000L
    public void runLongMethodComposedWithTimeout() {
        testScriptRepository.doThirdLongJob(10_000L);
        fail("Long-running methods must throw exception if timeout is set");
    }


}
