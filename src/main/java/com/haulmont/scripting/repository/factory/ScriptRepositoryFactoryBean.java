package com.haulmont.scripting.repository.factory;

import com.haulmont.scripting.repository.ScriptMethod;
import com.haulmont.scripting.repository.ScriptParam;
import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.config.ScriptInfo;
import com.haulmont.scripting.repository.executor.ScriptExecutor;
import com.haulmont.scripting.repository.provider.ScriptProvider;
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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class that creates proxies for script repositories based on configuration data. Proxies will forward script repository interface method
 * invocations to get script text from providers and then for evaluation to actual executor class.
 *
 * Factory scans packages and creates script repository proxies when context initialization is finished.
 *
 * @see BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)
 */
public class ScriptRepositoryFactoryBean implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    public static final String NAME = "scriptRepositoryFactory";

    private static final Logger log = LoggerFactory.getLogger(ScriptRepositoryFactoryBean.class);

    private final List<String> basePackages;

    private final Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig;

    private ApplicationContext ctx;

    /**
     * Creates factory bean definition and registers it in spring context. In case of double configuration XML and annotation
     * will merge configuration data - package names and custom annotation configuration.
     * @param registry Spring bean definition registry
     * @param basePackages list of base package names to scan for script repositories.
     * @param customAnnotationsConfig configuration for custom annotations.
     * @return proxy factory bean definition that was registered in spring context.
     */
    @SuppressWarnings("unchecked")//to avoid warnings on constructor argument cast
    public static BeanDefinition registerBean(BeanDefinitionRegistry registry, List<String> basePackages, Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
        BeanDefinition beanDefinition;
        if (!registry.containsBeanDefinition(ScriptRepositoryFactoryBean.NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ScriptRepositoryFactoryBean.class);
            builder.addConstructorArgValue(basePackages);
            builder.addConstructorArgValue(customAnnotationsConfig);
            beanDefinition = builder.getBeanDefinition();
            registry.registerBeanDefinition(ScriptRepositoryFactoryBean.NAME, beanDefinition);
        } else {
            beanDefinition = registry.getBeanDefinition(ScriptRepositoryFactoryBean.NAME);
            List<String> basePackagesArg = (List<String>) beanDefinition.getConstructorArgumentValues().getArgumentValue(0, List.class).getValue();
            basePackagesArg.addAll(basePackages);
            Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsArg =
                    (Map<Class<? extends Annotation>, ScriptInfo>) beanDefinition.getConstructorArgumentValues().getArgumentValue(1, Map.class).getValue();
            customAnnotationsArg.putAll(customAnnotationsConfig);
        }
        return beanDefinition;
    }

    /**
     * Factory constructor.
     * @param basePackages list of base package names to scan for script repositories.
     * @param customAnnotationsConfig configuration for custom annotations.
     */
    public ScriptRepositoryFactoryBean(List<String> basePackages, Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
        this.basePackages = basePackages;
        this.customAnnotationsConfig = customAnnotationsConfig;
    }

    /**
     * Register script repository interfaces instances as beans so we can inject them into application. All interface instances
     * will be created in a lazy manner using factory method.
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
                    registry.registerBeanDefinition(definition.getBeanClassName(), definition);
                }
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException(e.getMessage(), e);
            }
        }
    }

    /**
     * Empty method - nothing to do in our case.
     * @see BeanDefinitionRegistryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * Sets application context - we'll need it to get provider and executor beans dynamically on method invocation.
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    /**
     * Factory method that creates proxies based on script repository interface and configuration.
     * @param repositoryClass script repository interface class.
     * @param customAnnotationsConfig custom annotation configurations for script execution.
     * @param <T> repository class type.
     * @return proxy that implements script repository interface.
     */
    @SuppressWarnings({"unchecked", "unused"})
    <T> T createRepository(Class<T> repositoryClass, Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
        if (!repositoryClass.isAnnotationPresent(ScriptRepository.class)) {
            throw new IllegalArgumentException("Script repositories must be annotated with @ScriptRepository.");
        }

        log.debug("Creating proxy for {}", repositoryClass.getName());
        RepositoryMethodsHandler handler = new RepositoryMethodsHandler(customAnnotationsConfig, ctx);
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass}, handler);
    }

    /**
     * Custom bean candidate provider that includes only annotated interfaces.
     * @see ClassPathScanningCandidateComponentProvider
     */
    static class ScriptRepositoryCandidateProvider extends ClassPathScanningCandidateComponentProvider {

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

    /**
     * Class that process all repository invocations. All method invocations that are not specific to script
     * repository interface (equals, hashcode, etc.) will be redirected to Object class instance created within the class.
     * Scripted method invocation configuration (method, provider bean instance and executor bean instance) are cached.
     */
    static class RepositoryMethodsHandler implements InvocationHandler, Serializable {

        private static final Logger log = LoggerFactory.getLogger(RepositoryMethodsHandler.class);

        private final Object defaultDelegate = new Object();

        private Map<Method, MethodInvocationInfo> methodScriptInfoMap = new ConcurrentHashMap<>();

        private final Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig;

        private final ApplicationContext ctx;

        RepositoryMethodsHandler(Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig, ApplicationContext ctx) {
            this.customAnnotationsConfig = customAnnotationsConfig;
            this.ctx = ctx;
        }

        /**
         * Main method that process script repository methods invocations.
         * On the first stage it checks if the method is scripted by checking annotation
         * (either ScriptMethod or pre-configured one) presence. If the method is not scripted, its invocation
         * is delegated to an Object instance. Otherwise we get script provider, script executor and
         * let them do their work.
         * @see InvocationHandler#invoke(Object, Method, Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!isScriptedMethod(method)) {
                return method.invoke(defaultDelegate, args);
            }
            log.debug("Class: {}, Proxy: {}, Method: {}, Args: {}", method.getDeclaringClass().getName(), proxy.getClass(), method.getName(), args);
            MethodInvocationInfo invocationInfo =
                    methodScriptInfoMap.computeIfAbsent(method, m ->
                    {
                        log.trace("Creating invocation info for method {} ", method.getName());
                        ScriptInfo scriptInfo = createMethodInfo(method);
                        log.trace("Script annotation class name: {}, provider: {}, executor: {}", scriptInfo.scriptAnnotation.getName(), scriptInfo.provider, scriptInfo.executor);
                        ScriptProvider provider = ctx.getBean(scriptInfo.provider, ScriptProvider.class);
                        ScriptExecutor executor = ctx.getBean(scriptInfo.executor, ScriptExecutor.class);
                        return new MethodInvocationInfo(provider, executor);
                    });

            Map<String, Object> binds = createParameterMap(method, args);

            String script = invocationInfo.provider.getScript(method);
            return invocationInfo.executor.eval(script, binds);
        }

        /**
         * Checks whether or not this method is scripted. It should be either annotated with
         * ScriptMethod annotation or annotation annotated with ScriptMethod or custom annotation should be configured in XML.
         * @param method method to be checked.
         * @return true if method should be executed as scripted method.
         */
        private boolean isScriptedMethod(Method method) {
            Annotation[] methodAnnotations = method.getAnnotations();
            Set<Class<?>> annotClasses = Arrays.stream(methodAnnotations).map(Annotation::annotationType).collect(Collectors.toSet());
            boolean match = customAnnotationsConfig.keySet().stream().anyMatch(annotClasses::contains);
            ScriptMethod annot = AnnotationUtils.getAnnotation(method, ScriptMethod.class);
            return annot != null || match;
        }

        /**
         * Creates method invocation data - annotation and provider and executor names.
         * @param method method that should be processed.
         * @return scripted method configuration.
         * @throws BeanCreationException if method is neither annotated nor configured.
         */
        private ScriptInfo createMethodInfo(Method method) throws BeanCreationException {
            ScriptMethod annotationConfig = AnnotationUtils.getAnnotation(method, ScriptMethod.class);
            if (annotationConfig != null) { //If method is configured with annotations
                String provider = annotationConfig.providerBeanName();
                String executor = annotationConfig.executorBeanName();
                return new ScriptInfo(ScriptMethod.class, provider, executor);
            } else { //Method is configured in XML
                Annotation[] methodAnnotations = method.getAnnotations();
                Set<Class<? extends Annotation>> annotClasses = Arrays.stream(methodAnnotations).map(Annotation::annotationType).collect(Collectors.toSet());
                Class<?> annotation = annotClasses.stream().filter(customAnnotationsConfig.keySet()::contains)
                        .findAny().orElseThrow(() -> new BeanCreationException(String.format("A method %s is not a scripted method. Annotation is not configured in XML", method.getName())));
                return customAnnotationsConfig.get(annotation);
            }
        }

        /**
         * Creates parameters map based on configured parameter names and actual argument values.
         * @param method called method.
         * @param args actual argument values.
         * @return parameter name - value maps.
         */
        private Map<String, Object> createParameterMap(Method method, Object[] args) {
            String[] argNames = Arrays.stream(method.getParameters())
                    .map(getParameterName())
                    .toArray(String[]::new);
            if (argNames.length != args.length) {
                throw new IllegalArgumentException(String.format("Parameters and args must be the same length. Parameters: %d args: %d", argNames.length, args.length));
            }
            return IntStream.range(0, argNames.length).boxed().
                    collect(Collectors.toMap(i -> argNames[i], i -> args[i]));
        }

        /**
         * Returns parameter name for a method.
         * @return parameter name.
         */
        private Function<Parameter, String> getParameterName() {
            return p -> p.isAnnotationPresent(ScriptParam.class)
                    ? p.getAnnotation(ScriptParam.class).value()
                    : p.getName();
        }

        /**
         * Structure for caching method invocation information.
         */
        class MethodInvocationInfo {
            final ScriptProvider provider;
            final ScriptExecutor executor;

            MethodInvocationInfo(ScriptProvider provider, ScriptExecutor executor) {
                this.provider = provider;
                this.executor = executor;
            }
        }
    }

}
