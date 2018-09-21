package com.company.rnd.scriptrepo.core.test.files;

import com.company.rnd.scriptrepo.repository.config.EnableScriptRepositories;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableScriptRepositories(basePackages = {"com.company.rnd.scriptrepo.core.test.files"})
public class FileRepositoryTestConfig {
}
