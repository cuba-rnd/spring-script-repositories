package com.company.rnd.scriptrepo.core.test.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TestTaxService {

    private static final Logger log = LoggerFactory.getLogger(TestTaxService.class);

    @Autowired
    private TestTaxCalculator taxCalculator;

    public BigDecimal calculateTaxAmount(BigDecimal sum){
        return taxCalculator.calculateTax(sum);
    }

}
