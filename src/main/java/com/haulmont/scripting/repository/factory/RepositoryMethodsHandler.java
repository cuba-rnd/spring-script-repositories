package com.haulmont.scripting.repository.factory;

import com.haulmont.scripting.repository.ScriptMethod;
import com.haulmont.scripting.repository.config.AnnotationConfig;
import com.haulmont.scripting.repository.evaluator.EvaluationStatus;
import com.haulmont.scripting.repository.evaluator.ScriptEvaluationException;
import com.haulmont.scripting.repository.evaluator.ScriptResult;
import com.haulmont.scripting.repository.evaluator.TimeoutAware;
import com.haulmont.scripting.repository.provider.ScriptNotFoundException;
import com.haulmont.scripting.repository.provider.ScriptProvider;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.ScriptSource;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class that process all repository invocations. All method invocations that are not specific to script
 * repository interface (equals, hashcode, etc.) will be redirected to Object class instance created within the class.
 * Scripted method invocation configuration (method, provider bean instance and evaluator bean instance) are cached.
 */
class RepositoryMethodsHandler implements InvocationHandler, Serializable {

    private final Logger log = LoggerFactory.getLogger(RepositoryMethodsHandler.class);

    private final Object defaultDelegate = new Object();

    private final Class<?> repositoryClass;
    private final Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig;

    private final ApplicationContext ctx;

    private Map<Method, AnnotationConfig> methodScriptInvocationMetadata = new ConcurrentHashMap<>(); //global invocation cache


    RepositoryMethodsHandler(Class<?> repositoryClass, ApplicationContext ctx, Map<Class<? extends Annotation>, AnnotationConfig> customAnnotationsConfig) {
        this.ctx = ctx;
        this.repositoryClass = repositoryClass;
        this.customAnnotationsConfig = customAnnotationsConfig;
        List<Method> scriptedMethods = Arrays.stream(repositoryClass.getMethods())
                .filter(this::isScriptedMethod)
                .collect(Collectors.toList());
        scriptedMethods.forEach(method -> methodScriptInvocationMetadata.computeIfAbsent(method, this::getAnnotationConfig));
    }

    /**
     * Main method that process script repository methods invocations.
     * On the first stage it checks if the method is scripted by checking annotation
     * (either ScriptMethod or pre-configured one) presence. If the method is not scripted, its invocation
     * is delegated to an Object instance. Otherwise we get script provider, script evaluator and
     * let them do their work.
     *
     * @see InvocationHandler#invoke(Object, Method, Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.trace("Class: {}, Proxy: {}, Method: {}, Args: {}",
                method.getDeclaringClass().getName(), proxy.getClass(), method.getName(), args);

        if (!isScriptedMethod(method)) {
            return method.invoke(defaultDelegate, args);
        }

        AnnotationConfig invocationInfo = methodScriptInvocationMetadata.get(method);

        long timeout = invocationInfo.timeout;

        log.trace("Submitting task for execution, timeout: {} method: {}", timeout, method);

        CompletableFuture<Object> scriptExecutionChain = null;
        ScriptProvider provider = (ScriptProvider) ctx.getBean(invocationInfo.provider);
        ScriptEvaluator evaluator = (ScriptEvaluator) ctx.getBean(invocationInfo.evaluator);
        Map<String, Object> binds = invocationInfo.createParameterMap(method, args);

        try {
            scriptExecutionChain = CompletableFuture
                    .supplyAsync(() -> provider.getScript(method))
                    .thenApply(scriptSource -> executeScriptedMethod(scriptSource, method, binds, evaluator))
                    .exceptionally(throwable -> tryDefaultMethod(throwable, method, args, repositoryClass));

            if (timeout > 0) {
                return scriptExecutionChain.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                return scriptExecutionChain.get();
            }
        } catch (Throwable ex) {
            if (scriptExecutionChain != null && !scriptExecutionChain.isDone()) {
                scriptExecutionChain.completeExceptionally(ex);
                cancelExecution(invocationInfo, provider, evaluator);
            }
            ScriptEvaluationException evaluationException = new ScriptEvaluationException("Error during script execution", ex);
            if (shouldWrapResult(method)) {
                return new ScriptResult<>(null, EvaluationStatus.FAILURE, evaluationException);
            }
            throw evaluationException;
        }
    }

    private void cancelExecution(AnnotationConfig invocationInfo, ScriptProvider provider, ScriptEvaluator evaluator) {
        if (ctx.isPrototype(invocationInfo.provider) && provider instanceof TimeoutAware) {
            log.trace("Cancelling provider {} ", invocationInfo.provider);
            ((TimeoutAware)provider).cancel();
        }
        if (ctx.isPrototype(invocationInfo.evaluator) && evaluator instanceof TimeoutAware) {
            log.trace("Cancelling evaluator {} ", invocationInfo.provider);
            ((TimeoutAware)evaluator).cancel();
        }
    }

    private Object executeScriptedMethod(ScriptSource script, Method method, Map<String, Object> binds, ScriptEvaluator evaluator) {
        Object scriptResult = evaluator.evaluate(script, binds);
        if (shouldWrapResult(method)) {
            return new ScriptResult<>(scriptResult, EvaluationStatus.SUCCESS, null);
        } else {
            return scriptResult;
        }
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
     * Creates method invocation data - annotation and provider and evaluator names.
     *
     * @param method method that should be processed.
     * @return scripted method configuration.
     * @throws IllegalStateException if method is neither annotated nor configured.
     */
    private AnnotationConfig getAnnotationConfig(Method method) {

        //Getting timeout from method's direct (1st level) annotations
        //Timeout specified at 1st level annotations will prevail over indirect one,
        //In absence of direct timeout we need to check
        // if we have indirect timeout set in @ScriptMethod or in XML config
        Long methodTimeout = getTimeout(method);

        ScriptMethod annotation = AnnotationUtils.getAnnotation(method, ScriptMethod.class);

        if (annotation == null) { //Annotation is configured in XML
            Annotation[] methodAnnotations = method.getAnnotations();

            Class<?> annotationInXml = Arrays
                    .stream(methodAnnotations)
                    .map(Annotation::annotationType)
                    .filter(customAnnotationsConfig.keySet()::contains)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException (
                            String.format("A method %s is not a scripted method. Annotation is not configured in XML"
                                    , method.getName())));

            AnnotationConfig annotationXmlConfig = customAnnotationsConfig.get(annotationInXml);

            Long timeout = methodTimeout != null ? methodTimeout : annotationXmlConfig.timeout;

            return new AnnotationConfig(ScriptMethod.class,
                    annotationXmlConfig.provider,
                    annotationXmlConfig.evaluator,
                    timeout,
                    annotationXmlConfig.description);
        } else { //If method is configured with custom annotation annotated with ScriptMethod

            Long timeout = methodTimeout != null ? methodTimeout : annotation.timeout();

            return new AnnotationConfig(ScriptMethod.class,
                    annotation.providerBeanName(),
                    annotation.evaluatorBeanName(),
                    timeout,
                    annotation.description());
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
     * @param cause          Why this method was called.
     * @param method         Interface default method to be invoked.
     * @param args           Method's arguments.
     * @param interfaceClass interface which default method to be invoked.
     * @return Default interface method invocation result.
     * @throws UnsupportedOperationException in case default method is not found.
     * @link https://blog.jooq.org/2018/03/28/correct-reflective-access-to-interface-default-methods-in-java-8-9-10/
     */
    private Object tryDefaultMethod(Throwable cause, Method method, Object[] args, Class<?> interfaceClass) {

        if (!(cause instanceof ScriptNotFoundException || cause.getCause() instanceof ScriptNotFoundException)) {
            throw new UnsupportedOperationException(
                    String.format("Error executing default method %s", method), cause);
        }

        if (!method.isDefault()) {
            throw new UnsupportedOperationException(
                    String.format("Method %s should have either script implementation or be default", method));
        }

        try {
            Object typedProxyWithDefaultMethod = Reflect.on(new Object()).as(interfaceClass);
            Method defaultMethod = interfaceClass.
                    getMethod(method.getName(), method.getParameterTypes());
            return defaultMethod.invoke(typedProxyWithDefaultMethod, args);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new UnsupportedOperationException(String.format("Default method %s cannot be invoked on %s: %s"
                    , method.getName(), interfaceClass.getName(), e.getMessage())
                    , e);
        }
    }

    private boolean shouldWrapResult(Method method) {
        return ScriptResult.class.isAssignableFrom(method.getReturnType());
    }


    Map<Method, AnnotationConfig> getMethodScriptInvocationMetadata() {
        return Collections.unmodifiableMap(methodScriptInvocationMetadata);
    }
}
