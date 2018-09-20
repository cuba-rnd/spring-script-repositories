package com.company.rnd.scriptrepo.core.test.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;

@Service
public class TestTaxService {

    private static final Logger log = LoggerFactory.getLogger(TestTaxService.class);

    @Inject
    private TestTaxCalculator taxCalculator;

    public BigDecimal calculateTaxAmount(BigDecimal sum){
        return taxCalculator.calculateTax(sum);
    }

}
