package com.company.rnd.scriptrepo.core.test.mixed;

import com.company.rnd.scriptrepo.core.test.files.Customer;
import com.company.rnd.scriptrepo.repository.ScriptParam;
import com.company.rnd.scriptrepo.repository.ScriptRepository;

import java.util.Date;
import java.util.UUID;

@ScriptRepository
public interface MixedConfigScriptRepository {

    @XmlGroovyScript
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);

    @AnnotatedGroovyScript
    Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);

}
