package com.haulmont.scripting.core.test.timeout;

import com.haulmont.scripting.repository.ScriptMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ScriptMethod(timeout = 100L)
public @interface TestComposedTimeout {
    long timeout() default -1L;
}
