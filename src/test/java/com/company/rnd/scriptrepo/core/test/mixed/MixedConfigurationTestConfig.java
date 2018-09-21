package com.company.rnd.scriptrepo.core.test.mixed;

import com.company.rnd.scriptrepo.repository.config.EnableScriptRepositories;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableScriptRepositories(basePackages = {"com.company.rnd.scriptrepo.core.test.mixed"})
public class MixedConfigurationTestConfig {
}
