package com.company.rnd.scriptrepo.core.test.database;

import com.company.rnd.scriptrepo.repository.ScriptParam;
import com.company.rnd.scriptrepo.repository.ScriptRepository;

import java.math.BigDecimal;

@ScriptRepository
public interface TestTaxCalculator {

    @DbGroovyScript
    BigDecimal calculateTax(@ScriptParam("amount") BigDecimal amount);

}
