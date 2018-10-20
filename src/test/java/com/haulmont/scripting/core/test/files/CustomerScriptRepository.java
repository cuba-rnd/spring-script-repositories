package com.haulmont.scripting.core.test.files;

import com.haulmont.scripting.repository.ScriptMethod;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface CustomerScriptRepository {

    @ScriptMethod
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @GroovyScript
    Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

    @ScriptMethod
    default String getDefaultName() {
        return "NewCustomer";
    }

    @ScriptMethod
    String getDefaultError();

    @ScriptMethod
    String sayHello();

}
