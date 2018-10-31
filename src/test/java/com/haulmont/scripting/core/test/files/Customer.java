package com.haulmont.scripting.core.test.files;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface Customer {

    UUID getId();
    String getName();
    Date getBirthDate();
    List<String> getMyData();

}
