package com.haulmont.scripting.core.test.js;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.haulmont.scripting.core.test.files.Customer;
import com.haulmont.scripting.core.test.files.CustomerScriptRepository;
import com.haulmont.scripting.repository.factory.ScriptInvocationMetadata;
import com.haulmont.scripting.repository.factory.ScriptRepositoryFactoryBean;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        int result = repo.simpleMath(1, 2);
        assertEquals(3, result);
    }

}