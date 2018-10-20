package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

@Component("appResourceProvider")
public class AppResourceProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(AppResourceProvider.class);

    @Value("${script.source.root.path}")
    private String rootPath;

    @Value("${script.source.file.extension}")
    private String fileExtension;

    @Override
    public ScriptSource getScript(Method method) {
        String path = rootPath + "/" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + fileExtension;
        log.debug("Getting file from resource {}", path);
        try {
            File f =  ResourceUtils.getFile(path);
            String text = String.join(System.lineSeparator(), Files.readAllLines(f.toPath(), StandardCharsets.UTF_8));
            return new ScriptSource(text, SourceStatus.FOUND, null);
        } catch (FileNotFoundException | AccessDeniedException e) {
            return new ScriptSource(null, SourceStatus.NOT_FOUND, e);
        } catch (Exception e) {
            return new ScriptSource(null, SourceStatus.FAILURE, e);
        }
    }
}
