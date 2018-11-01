package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.ResourceUtils;

import java.lang.reflect.Method;

/**
 * Loads script text from application using {@link ResourceUtils#getFile(String)} class.
 * Allows you to define your own way of building resource path based on method signature.
 */
public abstract class AbstractResourceProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractResourceProvider.class);

    private DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptSource getScript(Method method) {
        String path = getResourcePath(method);
        log.debug("Getting script from resource {}", path);
        Resource res = resourceLoader.getResource(path);
        if (res.exists()){
            return new ResourceScriptSource(res);
        } else {
            throw new ScriptNotFoundException(String.format("Resource %s does not exists", path));
        }
    }

    /**
     * Creates resource path string based on method signature.
     * @param method scripted method.
     * @return resource path string.
     */
    public abstract String getResourcePath(Method method);

}
