package com.company.rnd.scriptrepo.core.test.database;

import com.company.rnd.scriptrepo.core.test.files.FileRepositoryTest;
import com.company.rnd.scriptrepo.test.entity.PersistentScript;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryTest.class);

    private TestTaxService taxService;
    private PersistentScript script;

    @Before
    public void setUp() throws Exception {

        script.setName("TestTaxCalculator.calculateTax");
        script.setSourceText("return amount*0.13");

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTaxCalculation () {
        Assert.assertTrue(taxService.calculateTaxAmount(BigDecimal.TEN).compareTo(BigDecimal.valueOf(1.4)) < 0);
    }

}
