package com.company.rnd.scriptrepo.core.test.database;

import com.company.rnd.scriptrepo.AppTestContainer;
import com.company.rnd.scriptrepo.core.test.files.FileRepositoryTest;
import com.company.rnd.scriptrepo.entity.PersistentScript;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class DatabaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryTest.class);

    @ClassRule
    public static AppTestContainer cont = AppTestContainer.Common.INSTANCE;

    private Metadata metadata;
    private Persistence persistence;
    private TestTaxService taxService;
    private PersistentScript script;

    @Before
    public void setUp() throws Exception {
        metadata = cont.metadata();
        persistence = cont.persistence();
        persistence.setSoftDeletion(false);
        taxService = AppBeans.get(TestTaxService.class);

        script = metadata.create(PersistentScript.class);
        script.setName("TestTaxCalculator.calculateTax");
        script.setSourceText("return amount*0.13");

        persistence.runInTransaction(em -> em.persist(script));
    }

    @After
    public void tearDown() throws Exception {
        persistence.runInTransaction(em -> em.remove(script));
    }

    @Test
    public void testTaxCalculation () {
        Assert.assertTrue(taxService.calculateTaxAmount(BigDecimal.TEN).compareTo(BigDecimal.valueOf(1.4)) < 0);
    }

}
