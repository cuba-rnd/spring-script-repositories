package com.company.rnd.scriptrepo.repository.factory;

import com.company.rnd.scriptrepo.repository.ScriptMethod;
import com.company.rnd.scriptrepo.repository.ScriptParam;
import com.company.rnd.scriptrepo.repository.ScriptRepository;
import com.company.rnd.scriptrepo.repository.factory.ScriptRepositoryConfigurationParser.ScriptInfo;
import com.haulmont.cuba.core.sys.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
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

public class ScriptRepositoryFactoryBean implements BeanDefinitionRegistryPostProcessor {

    public static final String NAME = "scriptRepositoryFactory";

    private static final Logger log = LoggerFactory.getLogger(ScriptRepositoryFactoryBean.class);

    private final List<String> basePackages;

    private final Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig;


    public ScriptRepositoryFactoryBean(List<String> basePackages, Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
        this.basePackages = basePackages;
        this.customAnnotationsConfig = customAnnotationsConfig;
    }

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


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @SuppressWarnings({"unchecked", "unused"})
    <T> T createRepository(Class<T> repositoryClass, Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
        if (!repositoryClass.isAnnotationPresent(ScriptRepository.class)) {
            throw new IllegalArgumentException("Script repositories must be annotated with @ScriptRepository.");
        }

        log.debug("Creating proxy for {}", repositoryClass.getName());
        RepositoryMethodsHandler handler = new RepositoryMethodsHandler(customAnnotationsConfig);
        return (T) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class<?>[]{repositoryClass}, handler);
    }

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

    static class RepositoryMethodsHandler implements InvocationHandler, Serializable {

        private static final Logger log = LoggerFactory.getLogger(RepositoryMethodsHandler.class);

        private final Object defaultDelegate = new Object();

        private Map<Method, MethodInvocationInfo> methodScriptInfoMap = new ConcurrentHashMap<>();

        private final Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig;

        RepositoryMethodsHandler(Map<Class<? extends Annotation>, ScriptInfo> customAnnotationsConfig) {
            this.customAnnotationsConfig = customAnnotationsConfig;
        }

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
                        log.trace("Script annotation class name: {}", scriptInfo.scriptAnnotation.getName());
                        log.trace("Provider bean name: {}", scriptInfo.provider);
                        ScriptProvider provider = (ScriptProvider) AppContext.getApplicationContext().getBean(scriptInfo.provider);
                        log.trace("Executor bean name: {}", scriptInfo.executor);
                        ScriptExecutor executor = (ScriptExecutor) AppContext.getApplicationContext().getBean(scriptInfo.executor);
                        return new MethodInvocationInfo(provider, executor);
                    });
            String[] paramNames = Arrays.stream(method.getParameters())
                    .map(getParameterName())
                    .toArray(String[]::new);
            String script = invocationInfo.provider.getScript(method);
            return invocationInfo.executor.eval(script, method, paramNames, args);
        }

        private boolean isScriptedMethod(Method method) {
            Annotation[] methodAnnotations = method.getAnnotations();
            Set<Class<?>> annotClasses = Arrays.stream(methodAnnotations).map(Annotation::annotationType).collect(Collectors.toSet());
            boolean match = customAnnotationsConfig.keySet().stream().anyMatch(annotClasses::contains);
            return method.isAnnotationPresent(ScriptMethod.class) || match;
        }

        private ScriptInfo createMethodInfo(Method method) throws BeanCreationException {
            if (method.isAnnotationPresent(ScriptMethod.class)) {
                ScriptMethod annotationConfig = method.getAnnotation(ScriptMethod.class);
                String provider = annotationConfig.providerBeanName();
                String executor = annotationConfig.executorBeanName();
                return new ScriptInfo(ScriptMethod.class, provider, executor);
            } else {
                Annotation[] methodAnnotations = method.getAnnotations();
                Set<Class<? extends Annotation>> annotClasses = Arrays.stream(methodAnnotations).map(Annotation::annotationType).collect(Collectors.toSet());
                Class<?> annotation = annotClasses.stream().filter(customAnnotationsConfig.keySet()::contains)
                        .findAny().orElseThrow(() -> new BeanCreationException(String.format("A method %s is not a scripted method. Annotation is not configured in XML", method.getName())));
                return customAnnotationsConfig.get(annotation);
            }
        }

        private Function<Parameter, String> getParameterName() {
            return p -> p.isAnnotationPresent(ScriptParam.class)
                    ? p.getAnnotation(ScriptParam.class).value()
                    : p.getName();
        }


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
