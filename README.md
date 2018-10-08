# Script Repository Interface 
Scripting in java applications is not a rare thing. Sometimes you need to extend your current business logic or 
add some application management logic. 

Scripting adds flexibility to an application, but also it adds some challenges:
1. Usually scripts are scattered along the application, so it is quite hard to manage numerous ```GroovyShell``` calls.
2. Scripts usually do not provide any information about input parameters: names and types as well as about output values.

The purpose of this library is to add some order into scripting extension points. 

The idea behind this library is simple. A developer creates an interface and links its methods to scripts using
annotations and two classes: script provider and script executor. Provider "knows" how to get script source text
and executor evaluates it and return results.

This approach adds "type-safety" to the process of passing script parameters and a developer will know what will be a
type of the script evaluation result. 

 The library provides the following:

Marker annotation for script repostitory interfaces.   
 ```java
public @interface ScriptRepository {
    String description() default "";
}
```
 
 Annotation to link interface method to a script source code. You need to provide bean names for script provider 
 bean and script executor bean. 
 ```java
public @interface ScriptMethod {
    String providerBeanName() default "groovyFileProvider";
    String executorBeanName() default "groovyJsrExecutor";
    String description() default "";
}
```

Interface for script provider:
```java
public interface ScriptProvider {
    String getScript(Method method);
}
```
The implementation should be able to find script source text based on scripted method's signature. As an example, the
library provides a default implementation ```GroovyScriptFileProvider``` for a provider that reads text files from a classpath. File name should 
have the same name as a method and the file should have ```.groovy``` extension.

Interface for script executor:
```java
public interface ScriptExecutor {
    <T> T eval(String script, Map<String, Object> parameters);
}

```
The implementation just uses script text and invokes it using parameters map. There is a default executor implementation 
```GroovyScriptJsrExecutor``` that uses JRE's JSR-223 engine to execute Groovy scripts. 

Since parameters names are important and java compiler erase actual parameter names from ```.class``` file (unless you 
enable "keep debug information" option during compilation), the library provides annotation for method parameters that 
let us to use meaningful parameter names in script instead of "arg0, arg1, etc." 
```java
public @interface ScriptParam {
    String value();
}
``` 
## Implementation
The library creates dynamic proxies for repository interfaces marked with ```@ScriptRepository``` annotation. All methods 
in this repository must be marked with ```@ScriptMethod```, default methods are not allowed. All interfaces marked 
with ```@ScriptRepository``` annotations will be published in spring's context and can be injected into other spring beans. 

When an interface method is called, the proxy invokes provider to get method script text and then executor to evaluate 
the result. 
## Configuration 
If you want to use default provider and executor then you need to import XML config from the library into your project:

```xml
    <import resource="classpath:com/haulmont/scripting/repository/**/script-repositories-config.xml"/>
```

In the project itself you can use two configuration options: annotations and XML. 

### Annotations configuration
To tell spring to search for script repositories you need to add ```@EnableSpringRepositories``` annotation to one of the 
configuration classes. For this annotation you need to specify array of package names that should be scanned.
```java
@Configuration
@EnableScriptRepositories(basePackages = {"com.example", "com.sample"})
public class ExampleConfig {
}
```
If you plan to use your own implementation for script provider and/or script executor (e.g. for JavaScript), you can specify 
their spring bean names in ```@ScriptMethod``` annotation:
```java
@ScriptMethod(providerBeanName = "jsFileProvider", executorBeanName = "jsExecutor")
```
To avoid copying and pasting this code across the project you can create your own annotation and use it in your project:
```java
@Target(ElementType.METHOD)
@ScriptMethod(providerBeanName = "jsFileProvider", executorBeanName = "jsExecutor")
public @interface JsScript {
}
``` 
### XML Configuration
You can also configure both package scanning and custom annotations using XML in spring configuration file:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:repo="http://www.cuba-platform.org/schema/script/repositories"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.3.xsd
        http://www.cuba-platform.org/schema/script/repositories http://www.cuba-platform.org/schema/script/repositories/script-repositories.xsd">

    <repo:script-repositories>
        <repo:base-packages>
            <repo:base-package>com.example</repo:base-package>
            <repo:base-package>com.sample</repo:base-package>
        </repo:base-packages>
        <repo:annotations-config>
            <repo:annotation-mapping annotation-class="com.example.JsScript"
                                     provider-bean-name="jsFileProvider"
                                     executor-bean-name="jsExecutor"/>
        </repo:annotations-config>
    </repo:script-repositories>

    <!-- Annotation-based beans -->
    <context:component-scan base-package="com.example.beans"/>

</beans>
``` 
### Mixed configuration
In case of mixed configuration - Annotations+XML, config parameters will be merged, therefore it is not recommended 
to configure the same custom annotation in two places because one of the configuration will override another.  

## Usage 
Let's assume that we use scripting library for customer management. In the simplest case script repository 
interface may look like this:
```java
@ScriptRepository
public interface CustomerScriptRepository {

    @ScriptMethod
    String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);
}
```
Then you need to create ```renameCustomer.groovy``` file and put it to the same package where the interface is located.
The script should use two parameters and return string. 

As an example:
```groovy
return "Customer with ${customerId} was renamed to ${newName}".toString()
``` 

That's almost it. After this you should enable script repositories feature using annotations or XML. After that you can use 
the repository above by injecting it to a bean and call its methods:

```java
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerScriptRepository repo;
    
    public String doRename(Customer c, String newName) {
        return repo.renameCustomer(c.getId(), newname);
    }
    
}
```
### More examples
You can find examples in test classes. They include custom script provider and custom annotation configuration.  

### References and thanks
There is a good [article](https://zeroturnaround.com/rebellabs/scripting-your-java-application-with-groovy/) by [Anton Arhipov](https://github.com/antonarhipov) that helped us a lot with implementation of this library.
