package com.company.rnd.scriptrepo.core.test.files;

import com.company.rnd.scriptrepo.repository.ScriptMethod;
import com.company.rnd.scriptrepo.repository.ScriptParam;
import com.company.rnd.scriptrepo.repository.ScriptRepository;
import com.haulmont.cuba.security.app.Authenticated;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface CustomerScriptRepository {

    @ScriptMethod
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @Authenticated //You may need this annotation to get scripts that might be protected by row-level security
    @ScriptMethod
    Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

}
