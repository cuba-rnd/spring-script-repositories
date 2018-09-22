package com.company.rnd.scriptrepo.repository.config;

import com.company.rnd.scriptrepo.repository.factory.ScriptRepositoryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that registers script repositories in Spring context if annotation-based configuration is user.
 * @see EnableScriptRepositories
 */
public class ScriptRepositoriesRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ScriptRepositoriesRegistrar.class);

    /**
     * @see ImportBeanDefinitionRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("{}", importingClassMetadata.getAnnotationTypes());
        if (importingClassMetadata.getAnnotationAttributes(getAnnotation().getName()) == null) {
            return;
        }

        AnnotationAttributes attributes = new AnnotationAttributes(importingClassMetadata.getAnnotationAttributes(getAnnotation().getName()));

        List<String> basePackages = Arrays.asList(attributes.getStringArray("basePackages"));

        Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig = new HashMap<>();//We need it to be not immutable in case XML config parser will add anything

        ScriptRepositoryFactoryBean.registerBean(registry, basePackages, customAnnotationsConfig);
    }

    /**
     * Method that returns annotation class that will be used to enable spring repositories configuration.
     * @return annotation class.
     */
    protected Class<? extends Annotation> getAnnotation() {
        return EnableScriptRepositories.class;
    }

}
