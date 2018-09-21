package com.company.rnd.scriptrepo.repository.config;

import com.company.rnd.scriptrepo.repository.factory.ScriptRepositoryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScriptRepositoriesRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ScriptRepositoriesRegistrar.class);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("{}", importingClassMetadata.getAnnotationTypes());
        if (importingClassMetadata.getAnnotationAttributes(getAnnotation().getName()) == null) {
            return;
        }

        AnnotationAttributes attributes = new AnnotationAttributes(importingClassMetadata.getAnnotationAttributes(getAnnotation().getName()));

        List<String> basePackages = Arrays.asList(attributes.getStringArray("basePackages"));

        Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig = Collections.emptyMap();

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ScriptRepositoryFactoryBean.class);
        builder.addConstructorArgValue(basePackages);
        builder.addConstructorArgValue(customAnnotationsConfig);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition(ScriptRepositoryFactoryBean.NAME, beanDefinition);
    }

    protected Class<? extends Annotation> getAnnotation() {
        return EnableScriptRepositories.class;
    }

}
