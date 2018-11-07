package com.haulmont.scripting.core.test.timeout;

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
import static org.junit.Assert.fail;

@ContextConfiguration(locations = {"classpath:com/haulmont/scripting/core/test/timeout/timeout-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeoutTest {

    @Autowired
    private TimeoutTestScriptRepository testScriptRepository;

    @Test//Timeout 1_000L
    public void runShortMethod() {
        String result = testScriptRepository.doLongJob(100L);
        assertEquals(testScriptRepository.SUCCESS, result);
    }

    @Test(expected = TimeoutException.class) //Timeout 1_000L
    public void runLongMethod() throws Throwable {
        try {
            testScriptRepository.doLongJob(10_000L);
        } catch (Throwable e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
        fail("Long-running methods must throw exception if timeout is set");
    }


    @Test(expected = TimeoutException.class) //Timeout 100L
    public void runLongMethodComposedDefault() throws Throwable {
        try {
            testScriptRepository.doAnotherLongJob(10_000L);
        } catch (Throwable e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
        fail("Long-running methods must throw exception if timeout is set");
    }


    @Test(expected = TimeoutException.class) //Timeout 1_000L
    public void runLongMethodComposedWithTimeout() throws Throwable {
        try {
            testScriptRepository.doThirdLongJob(10_000L);
        } catch (Throwable e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
        fail("Long-running methods must throw exception if timeout is set");
    }


}
