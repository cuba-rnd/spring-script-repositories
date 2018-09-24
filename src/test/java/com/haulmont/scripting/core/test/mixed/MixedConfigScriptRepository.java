package com.haulmont.scripting.core.test.mixed;

import com.haulmont.scripting.core.test.files.Customer;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface MixedConfigScriptRepository {

    @XmlGroovyScript
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @AnnotatedGroovyScript
    Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

}
