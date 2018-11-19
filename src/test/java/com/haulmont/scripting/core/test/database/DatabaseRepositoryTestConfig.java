package com.haulmont.scripting.core.test.database;

import com.haulmont.scripting.repository.evaluator.EvaluationStatus;
import com.haulmont.scripting.repository.evaluator.ScriptResult;
import org.hsqldb.jdbc.JDBCDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

@Configuration
@PropertySource({"classpath:com/haulmont/scripting/core/test/database/datasource.properties"})
public class DatabaseRepositoryTestConfig {

    @Value("${jdbc.url}")
    private String dbUrl;

    @Value("${jdbc.user}")
    private String user;

    @Value("${jdbc.password}")
    private String password;

    @Bean
    public JDBCDataSource dataSource(){
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setURL(dbUrl);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public TestTaxService testTaxService() {
        return new TestTaxService();
    }

    /**
     * Inner class just to keep all test classes in one place.
     */
    public static class TestTaxService {

        @Autowired
        private TestTaxCalculator taxCalculator;

        public BigDecimal calculateTaxAmount(BigDecimal sum){
            ScriptResult<BigDecimal> tax = taxCalculator.calculateTax(sum);
            if (tax.getStatus() != EvaluationStatus.FAILURE) {
                return tax.getValue();
            } else {
                throw new RuntimeException(tax.getError());
            }
        }

        public BigDecimal calculateVat(BigDecimal sum) {
            return taxCalculator.calculateVat(sum);
        }

        public String runNotImplementedMethod() {
            return  taxCalculator.notImplementedMethod();
        }

        public String runTimeoutError() {
            return  taxCalculator.timeoutError();
        }

    }
}
