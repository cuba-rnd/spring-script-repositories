package com.haulmont.scripting.core.test.mock;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Locale;

@ContextConfiguration(locations = {"classpath:com/haulmont/scripting/core/test/mock/mock-test-spring.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MockTest {

    @Tested
    private MockTestService testService;

    @Injectable
    private MockTestScriptRepository testScriptRepository;


    @Test
    public void invokeMockedMethod() {

        new Expectations() {
            {testScriptRepository.sayHello(Locale.ENGLISH); result = "Hi";}
            {testScriptRepository.sayHello(Locale.GERMAN); result = "Halo";}
        };

        Assert.assertEquals("Halo John", testService.sayHelloWithName("John", Locale.GERMAN));
        Assert.assertEquals("Hi John", testService.sayHelloWithName("John", Locale.ENGLISH));
    }


}
