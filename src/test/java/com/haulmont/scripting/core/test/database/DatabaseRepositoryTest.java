package com.haulmont.scripting.core.test.database;

import com.haulmont.scripting.repository.evaluator.ScriptEvaluationException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.fail;

@ContextConfiguration(locations = {"classpath:com/haulmont/scripting/core/test/database/db-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRepositoryTest.class);

    @Autowired
    private DatabaseRepositoryTestConfig.TestTaxService taxService;

    @Autowired
    private JDBCDataSource dataSource;

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = dataSource.getConnection();
        try (Statement st = conn.createStatement()){
            st.execute("create table persistent_script (name varchar(255) not null, source_text varchar(1000) not null)");
            st.execute("insert into persistent_script values('TestTaxCalculator.calculateTax', 'return amount*0.13')");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Statement st = conn.createStatement()){
            st.execute("drop table persistent_script");
            st.execute("shutdown");
        }
        conn.close();
    }

    @Test
    public void testTaxCalculation () {
        BigDecimal taxAmount = taxService.calculateTaxAmount(BigDecimal.TEN);
        log.info("Tax amount is: {}", taxAmount);
        Assert.assertTrue(BigDecimal.valueOf(1.4).compareTo(taxAmount) > 0);
    }


    @Test (expected = IllegalArgumentException.class)
    public void testConfigError() {
        taxService.calculateVat(BigDecimal.TEN);
        fail("Annotations not mapped in XML should not be handled");
    }

    @Test (expected = ScriptEvaluationException.class)
    public void testNotImplementedError() {
        taxService.runNotImplementedMethod();
        fail("When running not implemented method it should throw an error");
    }

    @Test (expected = ScriptEvaluationException.class)
    public void testDbTimeout() {
        taxService.runTimeoutError();
        fail("The method should fail due to timeout");
    }

}
