package com.haulmont.scripting.repository.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.haulmont.scripting.repository")
@PropertySource("classpath:script-repo.properties")
public class ScriptRepositoriesAutoConfiguration {

}
