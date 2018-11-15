package com.haulmont.scripting.core.test.database;

import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.evaluator.ScriptResult;

import java.math.BigDecimal;

@ScriptRepository
public interface TestTaxCalculator {

    @DbGroovyScript
    ScriptResult<BigDecimal> calculateTax(@ScriptParam("amount") BigDecimal amount);
}
