package com.company.rnd.scriptrepo.core.test.files;

import com.company.rnd.scriptrepo.AppTestContainer;
import com.haulmont.cuba.core.global.AppBeans;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryTest.class);

    @ClassRule
    public static AppTestContainer cont = AppTestContainer.Common.INSTANCE;

    private CustomerScriptRepository repo;

    @Before
    public void setUp() throws Exception {
        repo = AppBeans.get(CustomerScriptRepository.class);
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
        Date birthDate = DateUtils.parseDate("1988-12-14", new String[]{"yyyy-MM-dd"});
        Customer c = repo.createCustomer(newName, birthDate);
        assertEquals(newName, c.getName());
        assertEquals(birthDate, c.getBirthDate());
        assertNotNull(c.getId());
    }

}