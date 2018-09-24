package com.haulmont.scripting.core.test.database;

import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

import java.math.BigDecimal;

@ScriptRepository
public interface TestTaxCalculator {

    @DbGroovyScript
    BigDecimal calculateTax(@ScriptParam("amount") BigDecimal amount);

}
