package com.haulmont.scripting.core.test.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MockTestService {

    @Autowired
    private MockTestScriptRepository testScriptRepository;

    public String sayHelloWithName(String name, Locale locale) {
        return String.format("%s %s", testScriptRepository.sayHello(locale), name);
    }

}
