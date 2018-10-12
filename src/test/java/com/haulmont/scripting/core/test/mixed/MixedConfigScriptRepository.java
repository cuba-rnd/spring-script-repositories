package com.haulmont.scripting.core.test.mixed;

import com.haulmont.scripting.core.test.files.Customer;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.executor.ExecutionResult;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface MixedConfigScriptRepository {

    @XmlGroovyScript
    ExecutionResult<String> renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @AnnotatedGroovyScript
    ExecutionResult<Customer> createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

}
