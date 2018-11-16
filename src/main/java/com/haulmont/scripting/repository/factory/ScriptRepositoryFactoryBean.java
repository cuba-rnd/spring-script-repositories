package com.haulmont.scripting.repository.factory;

import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.config.AnnotationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that creates proxies for script repositories based on configuration data. Proxies will forward script repository interface method
 * invocations to get script text from providers and then for evaluation to actual evaluator class.
 * <p>
 * Factory scans packages and creates script repository proxies when context initialization is finished.
 *
 * @see BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
 */
public class ScriptRepositoryFactoryBean implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    public static final String NAME = "scriptRepositoryFactory";

    private static final Logger log = LoggerFactory.getLogger(ScriptRepositoryFactoryBean.class);

    private final List<String> basePackages;

    private Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig; //global custom annotations config

    private Map<Method, AnnotationConfig> methodScriptInvocationMetadata = new ConcurrentHashMap<>(); //global invocation cache

    private ApplicationContext ctx;

    /**
     * Creates factory bean definition and registers it in spring context. In case of double configuration XML and annotation
     * will merge configuration data - package names and custom annotation configuration.
     *
     * @param registry                Spring bean definition registry
     * @param basePackages            list of base package names to scan for script repositories.
     * @param customAnnotationsConfig configuration for custom annotations.
     * @return proxy factory bean definition that was registered in spring context.
     */
    @SuppressWarnings("unchecked")//to avoid warnings on constructor argument cast
    public static BeanDefinition registerBean(BeanDefinitionRegistry registry, List<String> basePackages, Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig) {
        BeanDefinition beanDefinition;
        if (!registry.containsBeanDefinition(ScriptRepositoryFactoryBean.NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ScriptRepositoryFactoryBean.class);
            builder.addConstructorArgValue(basePackages);
            builder.addConstructorArgValue(customAnnotationsConfig);
            beanDefinition = builder.getBeanDefinition();
            registry.registerBeanDefinition(ScriptRepositoryFactoryBean.NAME, beanDefinition);
            log.info("Added script repository factory bean: {}, annotations config {} ", beanDefinition.getBeanClassName(), customAnnotationsConfig);
        } else {
            beanDefinition = registry.getBeanDefinition(ScriptRepositoryFactoryBean.NAME);
            List<String> basePackagesArg = (List<String>) beanDefinition.getConstructorArgumentValues().getArgumentValue(0, List.class).getValue();
            basePackagesArg.addAll(basePackages);
            Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsArg =
                    (Map<Class<? extends Annotation>, AnnotationConfig>) beanDefinition.getConstructorArgumentValues().getArgumentValue(1, Map.class).getValue();
            customAnnotationsArg.putAll(customAnnotationsConfig);
            log.debug("Added configuration to script repository factory bean: {}", customAnnotationsConfig);
        }
        return beanDefinition;
    }

    /**
     * Factory constructor.
     *
     * @param basePackages            list of base package names to scan for script repositories.
     * @param customAnnotationsConfig configuration for custom annotations.
     */
    public ScriptRepositoryFactoryBean(List<String> basePackages, Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig) {
        this.basePackages = basePackages;
        this.customAnnotationsConfig = customAnnotationsConfig;
    }

    /**
     * Register script repository interfaces instances as beans so we can inject them into application. All interface instances
     * will be created in a lazy manner using factory method.
     *
     * @see ScriptRepositoryFactoryBean#createRepository(Class, Map)
     * @see BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathScanningCandidateComponentProvider provider
                = new ScriptRepositoryCandidateProvider();
        for (String packageName : basePackages) {
            Set<BeanDefinition> candidateComponents = provider.findCandidateComponents(packageName);
            try {
                for (BeanDefinition definition : candidateComponents) {
                    definition.setFactoryBeanName(NAME);
                    definition.setFactoryMethodName("createRepository");
                    definition.getConstructorArgumentValues().addGenericArgumentValue(Class.forName(definition.getBeanClassName()));
                    definition.getConstructorArgumentValues().addGenericArgumentValue(customAnnotationsConfig);
                    log.info("Registering script repository interface: {}", definition.getBeanClassName());
                    registry.registerBeanDefinition(definition.getBeanClassName(), definition);
                }
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException(e.getMessage(), e);
            }
        }
    }

    /**
     * Empty method - nothing to do in our case.
     *
     * @see BeanDefinitionRegistryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * Sets application context - we'll need it to get provider and evaluator beans dynamically on method invocation.
     *
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    /**
     * Factory method that creates proxies based on script repository interface and configuration.
     *
     * @param repositoryClass         script repository interface class.
     * @param customAnnotationsConfig custom annotation configurations for script execution.
     * @param <T>                     repository class type.
     * @return proxy that implements script repository interface.
     */
    @SuppressWarnings({"unused", "unchecked"})
    <T> T createRepository(Class<T> repositoryClass, Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig) {
        if (!repositoryClass.isAnnotationPresent(ScriptRepository.class)) {
            throw new IllegalArgumentException("Script repositories must be annotated with @ScriptRepository.");
        }

        log.debug("Creating proxy for {}", repositoryClass.getName());
        RepositoryMethodsHandler handler = new RepositoryMethodsHandler(repositoryClass, ctx, customAnnotationsConfig);
        methodScriptInvocationMetadata.putAll(handler.getMethodScriptInvocationMetadata());
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass}, handler);
    }

    public Map<Method, AnnotationConfig> getMethodInvocationsInfo() {
        return Collections.unmodifiableMap(methodScriptInvocationMetadata);
    }

    /**
     * Custom bean candidate provider that includes only annotated interfaces.
     *
     * @see ClassPathScanningCandidateComponentProvider
     */
    class ScriptRepositoryCandidateProvider extends ClassPathScanningCandidateComponentProvider {

        ScriptRepositoryCandidateProvider() {
            super(false);
            addIncludeFilter(new AnnotationTypeFilter(ScriptRepository.class));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isInterface() && metadata.isIndependent();
        }
    }


}
