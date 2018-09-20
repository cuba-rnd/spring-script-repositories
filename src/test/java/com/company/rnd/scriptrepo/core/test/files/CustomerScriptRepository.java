package com.company.rnd.scriptrepo.core.test.files;

import com.company.rnd.scriptrepo.repository.ScriptMethod;
import com.company.rnd.scriptrepo.repository.ScriptParam;
import com.company.rnd.scriptrepo.repository.ScriptRepository;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface CustomerScriptRepository {

    @ScriptMethod
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @ScriptMethod
    Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

}
