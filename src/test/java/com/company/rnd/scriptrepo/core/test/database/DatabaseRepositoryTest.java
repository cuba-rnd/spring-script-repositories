package com.company.rnd.scriptrepo.core.test.database;

import com.company.rnd.scriptrepo.core.test.files.FileRepositoryTest;
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

@ContextConfiguration(locations = {"classpath:test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryTest.class);

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
        Assert.assertTrue(taxService.calculateTaxAmount(BigDecimal.TEN).compareTo(BigDecimal.valueOf(1.4)) < 0);
    }

}
