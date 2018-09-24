package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Simple provider that gets script source from the same dir where Script
 * Repository interface is and file name equals to method name+'.groovy'.
 */
@Component("groovyFileProvider")
public class GroovyScriptFileProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptFileProvider.class);

    @Override
    public String getScript(Method method) {
        Class<?> scriptRepositoryClass = method.getDeclaringClass();
        String methodName = method.getName();
        String fileName = methodName + ".groovy";
        log.trace("Getting groovy script from file: {}", fileName);
        InputStream resourceAsStream = scriptRepositoryClass.getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
