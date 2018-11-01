# Script Repository Interface 
Scripting in java applications is not a rare thing. Sometimes you need to extend your current business logic or 
add some application management logic. It is very useful since business logic might not be well-defined at the moment 
of an application's development, or you need to change it frequently without redeploying the application.

Scripting adds flexibility to an application, but also it adds some challenges:
1. Usually scripts are scattered along the application, so it is quite hard to manage numerous ```GroovyShell``` calls.
2. Scripts usually do not provide any information about input parameters: names and types as well as about output values.

The purpose of this library is to add some order into scripting extension points. 

The idea behind this library is simple. A developer creates an interface and links its methods to scripts using
annotations.

This approach adds "type-safety" to the process of passing script parameters and a developer will know what will be a
type of the script evaluation result. 

## Usage
To start working with the script repositories, you need to do the following:

1. Specify library dependency in your project build file
    ```groovy
    compile('com.haulmont.scripting:spring-script-repositories:0.1-SNAPSHOT')
    ```
    Please note that the library's jar file should be placed near application jar files. 
    E.g. if you use tomcat, please put this file to deployed application's WEB-INF/lib folder not to tomcat shared libs. 
    We need it to use a correct classloader for proxy creation.
     
2. Define script repository interfaces 
    ```java
    @ScriptRepository
    public interface CustomerScriptRepository {
    
        @ScriptMethod
        String renameCustomer(@ScriptParam("customerId") UUID customerId, @ScriptParam("newName") String newName);
    
        @ScriptMethod
        Customer createCustomer(@ScriptParam("name") String name, @ScriptParam("birthDate") Date birthDate);
    }
    ```
    You can use default implementations in repository interfaces if you want to start quickly without writing scripts for methods.
    
3. Define root folder where your scripts will be located by defining ```groovy.script.source.root.path``` property in
```application.properties``` file:
    ```properties
    groovy.script.source.root.path=classpath:scripts
    ```
    Prefixes ```classpath:```, ```file:```, ```jar:``` can be used. If source root path is not specified, 
    the library will use default value: ```classpath:com/haulmont/scripting/scripts```
    
4. Implement scripts that should be executed and save them in script source root folder. By default, they should be named using pattern 
```InterfaceName.methodName.groovy```. So for the example described in p. 2 there will be two files:
    1. ```CustomerScriptRepository.renameCustomer.groovy```
    2. ```CustomerScriptRepository.createCustomer.groovy```
    
    In your scripts you can use parameters defined in interface's method signatures, parameter names should match those 
    defined in ```@ScriptParam``` annotation. For example, for method ```createCustomer``` script may look like the following:
    ```groovy
    Customer c = new Customer()
    c.setId(UUID.randomUUID())
    c.setName(name)
    c.setBirthDate(birthDate)
    return c
    ```
    Parameters ```name``` and ```birthdate``` will be substituted based on values passed by a caller. 
    
5. Enable scripting repositories in your application by adding ```@EnableScriptRepositories``` annotation to your 
application configuration and specify path list where your repository interfaces are located.
    ```java
    @Configuration
    @EnableScriptRepositories(basePackages = {"com.example", "com.sample"})
    public class ExampleConfig {
    }
    ```
6. Inject the interface into proper services and use it as "regular" bean.
    ```java
    public class CustomerService {
        
        @Autowired
        private CustomerScriptRepository customerScriptRepository;
    
    	public Customer createNew(String name, Date birthDate) {
 	         return customerScriptRepository.createCustomer(name, birthDate);
    	}    
    }

    ```     
So it should be pretty easy to get started with the library. 
By default, it supports groovy scripts, but it is quite easy to add any scripting language to it. Below is the 
explanation of the library's internals and configuration.  

## Internals

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
    String providerBeanName() default "groovyResourceProvider";
    String executorBeanName() default "groovyJsrEvaluator";
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
library provides a default implementation ```GroovyScriptFileProvider``` for a provider that reads text files from a source root. 

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
### More examples
You can find examples in test classes. They include custom script provider and custom annotation configuration.  

## Implementation
The library creates dynamic proxies for repository interfaces marked with ```@ScriptRepository``` annotation. All methods 
in this repository must be marked with ```@ScriptMethod```. All interfaces marked with ```@ScriptRepository``` annotation
 will be published in Spring's context and can be injected into other spring beans. 

When an interface method is called, the proxy invokes provider to get method script text and then executor to evaluate 
the result.

## Configuration 

In the project itself you can use two configuration options: annotations and XML. 

### Annotations configuration
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

### References and thanks
There is a good [article](https://zeroturnaround.com/rebellabs/scripting-your-java-application-with-groovy/) by [Anton Arhipov](https://github.com/antonarhipov) that helped us a lot with implementation of this library.
