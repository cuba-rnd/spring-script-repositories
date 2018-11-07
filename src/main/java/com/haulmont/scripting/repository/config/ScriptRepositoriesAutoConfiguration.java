package com.haulmont.scripting.repository.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = "com.haulmont.scripting.repository")
@PropertySource("classpath:script-repo.properties")
public class ScriptRepositoriesAutoConfiguration {

    public static final String THREAD_POOL_NAME = "scriptTaskExecutor";

    @Bean(name = THREAD_POOL_NAME)
    public ThreadPoolTaskExecutor scriptTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(0);
        return threadPoolTaskExecutor;
    }


}
