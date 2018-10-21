package com.haulmont.scripting.core.test.mixed;

import com.haulmont.scripting.repository.config.EnableScriptRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:com/haulmont/scripting/core/test/mixed/mixed-repo.properties")
@EnableScriptRepositories(basePackages = {"com.haulmont.scripting.core.test.mixed"})
public class MixedConfigurationTestConfig {
}
