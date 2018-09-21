package com.company.rnd.scriptrepo.core.test.files;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
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
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:com/company/rnd/scriptrepo/core/test/files/files-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class FileRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryTest.class);

    @Autowired
    private CustomerScriptRepository repo;

    @Before
    public void setUp() throws Exception {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRunSimpleScript() {
        UUID customerId = UUID.randomUUID();
        String newName = RandomStringUtils.randomAlphabetic(8);
        String s = repo.renameCustomer(customerId, newName);
        log.info(s);
        assertNotNull(s);
        assertTrue(s.contains(customerId.toString()));
        assertTrue(s.contains(newName));
    }

    @Test
    public void testCreateObject() throws ParseException {
        String newName = RandomStringUtils.randomAlphabetic(8);
        Date birthDate = DateUtils.parseDate("1988-12-14", "yyyy-MM-dd");
        Customer c = repo.createCustomer(newName, birthDate);
        assertEquals(newName, c.getName());
        assertEquals(birthDate, c.getBirthDate());
        assertNotNull(c.getId());
    }

}