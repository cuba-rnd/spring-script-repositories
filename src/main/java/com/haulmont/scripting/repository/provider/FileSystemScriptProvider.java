package com.haulmont.scripting.repository.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component("fileSystemProvider")
public class FileSystemScriptProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(FileSystemScriptProvider.class);

    final Environment env;

    @Autowired
    public FileSystemScriptProvider(Environment env) {
        this.env = env;
    }

    @Override
    public ScriptSource getScript(Method method) {
        String scriptRoot = env.getProperty("script.source.root");
        File f = new File(scriptRoot, method.getDeclaringClass().getSimpleName() + File.separator + method.getName()+".groovy");
        log.debug(f.getAbsolutePath());
        if (!f.exists() || !f.canRead()) {
            return new ScriptSource(null, SourceStatus.NOT_FOUND, null);
        }
        try {
            String text = String.join(System.lineSeparator(), Files.readAllLines(Paths.get(f.toURI())));
            return new ScriptSource(text, SourceStatus.FOUND, null);
        } catch (IOException e) {
            return new ScriptSource(null, SourceStatus.FAILURE, e);
        }
    }
}
