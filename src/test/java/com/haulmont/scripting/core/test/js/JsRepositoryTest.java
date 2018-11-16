package com.haulmont.scripting.core.test.js;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:com/haulmont/scripting/core/test/js/js-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class JsRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(JsRepositoryTest.class);

    @Autowired
    private JsScriptRepository repo;


    @Before
    public void setUp() throws Exception {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testSimpleMath() {
        double result = repo.simpleMath(1.1, 2.1);
        log.trace("JS result: {}", result);
        assertTrue(Math.abs(3.2 - result) < 0.0001);
    }

}