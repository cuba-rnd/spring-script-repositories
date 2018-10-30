package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * Loads script text from application using {@link ResourceUtils#getFile(String)} class.
 * Allows you to define your own way of building resource path based on method signature.
 */
public abstract class AppResourceProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(AppResourceProvider.class);

    private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptSource getScript(Method method) {
        String path = getResourcePath(method);
        log.debug("Getting file from resource {}", path);
        try {
            Resource res = resourceLoader.getResource(path);
            if (res.exists()){
                BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8));
                String text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                log.trace("Script found. Script text is:\n{}\n", text);
                return new ScriptSource(text, SourceStatus.FOUND, null);
            } else {
                throw new FileNotFoundException(String.format("File %s does not exists", path));
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            return new ScriptSource(null, SourceStatus.NOT_FOUND, e);
        } catch (Exception e) {
            return new ScriptSource(null, SourceStatus.FAILURE, e);
        }
    }

    /**
     * Creates resource path string based on method signature.
     * @param method scripted method.
     * @return resource path string.
     */
    public abstract String getResourcePath(Method method);

}
