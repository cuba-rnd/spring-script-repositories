package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

/**
 * Loads script text from application using {@link ResourceUtils#getFile(String)} class.
 * Allows you to define your own way of building resource path based on method signature.
 */
public abstract class AppResourceProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(AppResourceProvider.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptSource getScript(Method method) {
        String path = getResourcePath(method);
        log.debug("Getting file from resource {}", path);
        try {
            File f =  ResourceUtils.getFile(path);
            String text = String.join(System.lineSeparator(), Files.readAllLines(f.toPath(), StandardCharsets.UTF_8));
            log.trace("Script found. Script text is:\n{}\n", text);
            return new ScriptSource(text, SourceStatus.FOUND, null);
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
