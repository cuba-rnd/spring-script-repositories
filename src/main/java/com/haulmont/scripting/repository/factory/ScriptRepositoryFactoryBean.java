package com.haulmont.scripting.repository.factory;

import com.haulmont.scripting.repository.ScriptMethod;
import com.haulmont.scripting.repository.ScriptRepository;
import com.haulmont.scripting.repository.config.AnnotationConfig;
import com.haulmont.scripting.repository.executor.ExecutionStatus;
import com.haulmont.scripting.repository.executor.ScriptResult;
import com.haulmont.scripting.repository.provider.ScriptNotFoundException;
import com.haulmont.scripting.repository.provider.ScriptProvider;
import org.joor.Reflect;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.ScriptSource;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.haulmont.scripting.repository.config.ScriptRepositoriesAutoConfiguration.THREAD_POOL_NAME;

/**
 * Class that creates proxies for script repositories based on configuration data. Proxies will forward script repository interface method
 * invocations to get script text from providers and then for evaluation to actual executor class.
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

    private Map<Method, ScriptInvocationMetadata> methodScriptInvocationMetadata = new ConcurrentHashMap<>(); //global invocation cache

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
     * Sets application context - we'll need it to get provider and executor beans dynamically on method invocation.
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
        RepositoryMethodsHandler handler = new RepositoryMethodsHandler(repositoryClass, ctx);
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass}, handler);
    }

    public List<ScriptInvocationMetadata> getMethodInvocationsInfo() {
        return new ArrayList<>(methodScriptInvocationMetadata.values());
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

    /**
     * Class that process all repository invocations. All method invocations that are not specific to script
     * repository interface (equals, hashcode, etc.) will be redirected to Object class instance created within the class.
     * Scripted method invocation configuration (method, provider bean instance and executor bean instance) are cached.
     */
    class RepositoryMethodsHandler implements InvocationHandler, Serializable {

        private final Logger log = LoggerFactory.getLogger(RepositoryMethodsHandler.class);

        private final Object defaultDelegate = new Object();

        private final Class<?> repositoryClass;

        private final ApplicationContext ctx;

        RepositoryMethodsHandler(Class<?> repositoryClass, ApplicationContext ctx) {
            this.ctx = ctx;
            this.repositoryClass = repositoryClass;
            List<Method> scriptedMethods = Arrays.stream(repositoryClass.getMethods())
                    .filter(this::isScriptedMethod)
                    .collect(Collectors.toList());
            scriptedMethods.forEach(this::getMethodInvocationInfo);
        }

        /**
         * Main method that process script repository methods invocations.
         * On the first stage it checks if the method is scripted by checking annotation
         * (either ScriptMethod or pre-configured one) presence. If the method is not scripted, its invocation
         * is delegated to an Object instance. Otherwise we get script provider, script executor and
         * let them do their work.
         *
         * @see InvocationHandler#invoke(Object, Method, Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!isScriptedMethod(method)) {
                return method.invoke(defaultDelegate, args);
            }
            log.debug("Class: {}, Proxy: {}, Method: {}, Args: {}",
                    method.getDeclaringClass().getName(), proxy.getClass(), method.getName(), args);

            ScriptInvocationMetadata invocationInfo = getMethodInvocationInfo(method);

            ScriptSource script = null;

            try {
                script = invocationInfo.getProvider().getScript(method);
            } catch (ScriptNotFoundException e) {
                if (!method.isDefault()) {
                    throw new UnsupportedOperationException(
                            String.format("Method %s should have either script implementation or be default", method), e);
                }
            }

            Callable<Object> callableTask;
            final ScriptSource src = script;

            if (src != null) {
                log.trace("Executing scripted method {}", method);
                callableTask = () -> {
                    Map<String, Object> binds = invocationInfo.createParameterMap(method, args);
                    return executeScriptedMethod(invocationInfo, src, binds);
                };
            } else {
                log.trace("Executing default method {}", method);
                callableTask = () -> {
                    return executeDefaultMethod(method, args, repositoryClass);
                };
            }

            ThreadPoolTaskExecutor executorService = ctx.getBean(THREAD_POOL_NAME, ThreadPoolTaskExecutor.class);

            try {
                Long timeout = invocationInfo.getTimeout();
                Future<Object> future = executorService.submit(callableTask);
                log.trace("Submitting task for execution, timeout: {}", timeout);
                if (timeout > 0) {
                    return future.get(timeout, TimeUnit.MILLISECONDS);
                } else {
                    return future.get();
                }
            } catch (Throwable ex) {
                log.error("Error executing scripted method", ex);
                if (shouldWrapResult(invocationInfo)) {
                    return new ScriptResult<>(null, ExecutionStatus.FAILURE, ex);
                }
                throw ex;
            }


        }

        private Object executeScriptedMethod(ScriptInvocationMetadata invocationInfo, ScriptSource script, Map<String, Object> binds) {
            Object scriptResult;

            scriptResult = invocationInfo.getExecutor().evaluate(script, binds);

            if (shouldWrapResult(invocationInfo)) {
                return new ScriptResult<>(scriptResult, ExecutionStatus.SUCCESS, null);
            }
            return scriptResult;
        }


        /**
         * Creates method invocation info metadata and puts it to cache.
         *
         * @param method method to be invoked.
         * @return cached method invocation metadata.
         */
        private ScriptInvocationMetadata getMethodInvocationInfo(Method method) {
            return methodScriptInvocationMetadata.computeIfAbsent(method, m ->
            {
                log.trace("Creating invocation info for method {} ", m.getName());
                AnnotationConfig annotationConfig = getAnnotationConfig(m);
                log.trace("Script annotation class name: {}, provider: {}, executor: {}",
                        annotationConfig.scriptAnnotation.getName(), annotationConfig.provider, annotationConfig.executor);
                ScriptProvider provider = ctx.getBean(annotationConfig.provider, ScriptProvider.class);
                ScriptEvaluator executor = ctx.getBean(annotationConfig.executor, ScriptEvaluator.class);
                return new ScriptInvocationMetadata(m,
                        annotationConfig.provider, provider,
                        annotationConfig.executor, executor,
                        annotationConfig.timeout);
            });
        }

        /**
         * Checks whether or not this method is scripted. It should be either annotated with
         * ScriptMethod annotation or annotation annotated with ScriptMethod or custom annotation should be configured in XML.
         *
         * @param method method to be checked.
         * @return true if method should be executed as scripted method.
         */
        private boolean isScriptedMethod(Method method) {
            Annotation[] methodAnnotations = method.getAnnotations();
            Set<Class<?>> annotClasses = Arrays.stream(methodAnnotations)
                    .map(Annotation::annotationType)
                    .collect(Collectors.toSet());
            boolean match = customAnnotationsConfig.keySet().stream()
                    .anyMatch(annotClasses::contains);
            ScriptMethod annot = AnnotationUtils.getAnnotation(method, ScriptMethod.class);
            return annot != null || match;
        }

        /**
         * Creates method invocation data - annotation and provider and executor names.
         *
         * @param method method that should be processed.
         * @return scripted method configuration.
         * @throws BeanCreationException if method is neither annotated nor configured.
         */
        private AnnotationConfig getAnnotationConfig(Method method) throws BeanCreationException {

            //Getting timeout from method's direct (1st level) annotations
            //Timeout specified at 1st level annotations will prevail over indirect one,
            //In absence of direct timeout we need to check
            // if we have indirect timeout set in @ScriptMethod or in XML config
            Long methodTimeout = getTimeout(method);

            ScriptMethod annotationConfig = AnnotationUtils.getAnnotation(method, ScriptMethod.class);

            if (annotationConfig != null) { //If method is configured with custom annotation annotated with ScriptMethod

                Long timeout = methodTimeout != null ? methodTimeout : annotationConfig.timeout();

                return new AnnotationConfig(ScriptMethod.class,
                        annotationConfig.providerBeanName(),
                        annotationConfig.evaluatorBeanName(),
                        timeout,
                        annotationConfig.description());
            } else { //Annotation is configured in XML
                Annotation[] methodAnnotations = method.getAnnotations();

                Class<?> annotation = Arrays
                        .stream(methodAnnotations)
                        .map(Annotation::annotationType)
                        .filter(customAnnotationsConfig.keySet()::contains)
                        .findAny()
                        .orElseThrow(() -> new BeanCreationException(
                                String.format("A method %s is not a scripted method. Annotation is not configured in XML"
                                        , method.getName())));

                AnnotationConfig annotationXmlConfig = customAnnotationsConfig.get(annotation);

                Long timeout = methodTimeout != null ? methodTimeout : annotationXmlConfig.timeout;

                return new AnnotationConfig(ScriptMethod.class,
                        annotationXmlConfig.provider,
                        annotationXmlConfig.executor,
                        timeout,
                        annotationXmlConfig.description);
            }
        }

        private Long getTimeout(Method method) {
            return Arrays.stream(AnnotationUtils.getAnnotations(method))
                    .filter(ann -> AnnotationUtils.getAnnotationAttributes(ann).containsKey("timeout"))
                    .map(ann -> Long.parseLong(String.valueOf(AnnotationUtils.getValue(ann, "timeout"))))
                    .filter(value -> value > 0L)
                    .min(Long::compareTo)
                    .orElse(null);
        }

        /**
         * Default interface method invocation.
         *
         * @param method         Interface default method to be invoked.
         * @param args           Method's arguments.
         * @param interfaceClass interface which default method to be invoked.
         * @return Default interface method invocation result.
         * @throws NoSuchMethodException in case default method is not found.
         * @link https://blog.jooq.org/2018/03/28/correct-reflective-access-to-interface-default-methods-in-java-8-9-10/
         */
        private Object executeDefaultMethod(Method method, Object[] args, Class<?> interfaceClass) throws NoSuchMethodException {
            try {
                Object typedProxyWithDefaultMethod = Reflect.on(new Object()).as(interfaceClass);
                Method defaultMethod = interfaceClass.
                        getMethod(method.getName(), method.getParameterTypes());
                return defaultMethod.invoke(typedProxyWithDefaultMethod, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new UnsupportedOperationException(String.format("Default method %s cannot be invoked on %s: %s"
                        , method.getName(), interfaceClass.getName(), e.getMessage())
                        , e);
            }
        }

        private boolean shouldWrapResult(ScriptInvocationMetadata invocationInfo) {
            return ScriptResult.class.isAssignableFrom(invocationInfo.getMethod().getReturnType());
        }

    }

}
