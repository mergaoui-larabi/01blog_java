# SpringBootApplication

## introduction 

after intializing a spring app u get a src/main/java/com/example/demo
in this path u find a file named by the name of application lets call it app,java
inside u will see a class named App that contains the main method thats ur entry point to the springboot application
wich contains SpringApplication class
above the class declarition u will find an anotation @@SpringBootApplication

## excution 

- 1. your java code (.java)
- 2. maven compiles (.class)
- 3. app.jar (bytecode + application.properties/application.yml + static files)
- 4. jvm starts (run spring bytecode +  run your bytecode)
- 5. spring runs:
    - scans annotation (via reflection)
    - creates object (beans)
    - wires dependencies

## reflection

the jvm expose metadata for every loaded class such as 
- class name
- methods
- fields
- constructor
- annotations

spring use reflection by scannig the class in the target/classes/

- load the class
- check for annotations
- create object dynamicly
- inject dependencies

```java
    Class<?> className = Class.forName("com.example.app");
    if (className.isAnnotationPresent(Service.class)) {
    // it's a bean
    }
    Object bean = className.getDeclaredConstructor().newInstance();
    Constructor<?> ctor = className.getConstructors()[0];
    Object dependency = getBean(ctor.getParameterTypes()[0]);
```
## @SpringBootApplication

- @Documented
    - Any class using that annotation will show it in the generated documentation

- @Inherited
    - allow the subclasses to inheret the parent class annotationb without explicitly type them

- @Retention(RetentionPolicy.RUNTIME)
    - decides the scoop of the annotation by these options SOURCE/CLASS/RUNTIME
    - SOURCE discard the annotation after compalition
    - CLASS tells the compiler to store the class metadata in the .class file but doesnt allow access at runtime (default behaviour)
    - RUNTIME tells the compiler to store the class metadata in the .class and allows access at runtime via reflection (reflect api)

- @Target(ElementType.TYPE)
    - indicates the types in which an annotation interface is applicable TYPE allow class interface record enum

these are java.lang.annotation now lets explore org.springframework.context.annotation 

- @AnnotationBeanNameGenerator;
- @Bean;
- @ComponentScan;
- @ComponentScan.Filter;
- @Configuration;
- @FilterType;
- @AliasFor;

## SpringApplication

## Chronological order
- jvm load ur main method from the app class

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestServiceApplication  {

	public static void main(String[] args) {
		SpringApplication.run(TestServiceApplication.class, args);
	}
}
```
- then run SprinApplication.run this mehtod create an instance of SpringApplication internally by passing the TestServiceApplication.class
.class is an object that defines the class itself think of it like a metadata of the class that holds class name fields methods annotations ...
the class metadata passed to the SpringApplication is called primarySources note that spring accepts multiple source classes 
- the instance is created in this manner:

```java
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public SpringApplication(@Nullable ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "'primarySources' must not be null");
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
		this.properties.setWebApplicationType(WebApplicationType.deduce());
		this.bootstrapRegistryInitializers = new ArrayList<>(
				getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();
	}
```

so let's walk trough the 5 steps to intialize a SpringApplication :

1. verify that primary sources are not null because spring need a minimum of one source class then stores them in a set because spring doesnt want a dupplicated configuration.
2. discover the type of our spring app is SERVLET/REACTIVE/NONE servlet means classic webapp (tomcat/mvc) reactive means reactive web app (WebFlux) none means a cli app non-web.
3. when u run the springbootApp ur jvm may have jars like this :
    - your-app.jar
    - spring-boot.jar
    - spring-context.jar
    - spring-core.jar
    - etc
The JVM can load classes from all of them beside ur jar file contains resource files as we desscussed earlier these resource files may contains class paths thats implements class such BootstrapRegistryInitializer, ApplicationContextInitializer, ApplicationListener
so we load those classpaths classpaths my refer to a class used by a dependencie or one of ur own classes but 90% of times it a dependencie class or third party class after the jvm loading the class he Instantiate objects using reflection and return the an ArrayList of objects

```java

    Class<?> dynClass = Class.forName("com.example.dynClass");
    Object obj = dynClass.getConstructor().newInstance();

```

4. store the main application class by crawling in the stackframes at the moment the call stack is like this:
    - mainApplication.main()
    - -> SpringApplication.run()
    - -> constructor
    - -> deduceMainApplicationClass()

the stack at the top contains the currently excuted stack frame so it walks to the bottom to get the main class

- NOTE: that we intialize 3 types of arrays 
1. bootstrapRegistryInitializers the are all classes in the jar files that implements the BootstrapRegistryInitializer class these classes is for ultra-early setup spring want to excute them before the application context is intialized
2. applicationContextInitializer the are all classes in the jar files that implements the ApplicationContextInitializer class these classes are excuted after the application context is refreshed/started
3. applicationListener the are all classes in the jar files that implements the ApplicationListener class these classes are responsable to react to the events they listen to spring can emit a viarity of events such ApplicationStartingEvent	startup beginning, ApplicationEnvironmentPreparedEvent	env ready, ApplicationPreparedEvent	context prepared, ApplicationStartedEvent	startup finished

now the spring instance is well intialized let go to the next phase wich the run method:

```java
public ConfigurableApplicationContext run(String... args) {
		Startup startup = Startup.create();
		if (this.properties.isRegisterShutdownHook()) {
			SpringApplication.shutdownHook.enableShutdownHookAddition();
		}
		DefaultBootstrapContext bootstrapContext = createBootstrapContext();
		ConfigurableApplicationContext context = null;
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting(bootstrapContext, this.mainApplicationClass);
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
			Banner printedBanner = printBanner(environment);
			context = createApplicationContext();
			context.setApplicationStartup(this.applicationStartup);
			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			Duration timeTakenToStarted = startup.started();
			if (this.properties.isLogStartupInfo()) {
				new StartupInfoLogger(this.mainApplicationClass, environment).logStarted(getApplicationLog(), startup);
			}
			listeners.started(context, timeTakenToStarted);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			throw handleRunFailure(context, ex, listeners);
		}
		try {
			if (context.isRunning()) {
				listeners.ready(context, startup.ready());
			}
		}
		catch (Throwable ex) {
			throw handleRunFailure(context, ex, null);
		}
		return context;
	}
```

- this code has 4 major phases :
1. PREPARE STARTUP INFRASTRUCTURE
    1. create a timer that tracks how long the startup takes nothing functional yet
    ```java
    Startup startup = Startup.create();
    ```
    2. creating a shutdown jvm hook this crutial for safety because when jvm stops springApplication make sure that it shutdown gracefully
    ```java
    if (this.properties.isRegisterShutdownHook()) {
    SpringApplication.shutdownHook.enableShutdownHookAddition();
    }
    ```
    3. Bootstrap Context this is a temporary container wich holds early startup objects before having an application context 
    ```java
    DefaultBootstrapContext bootstrapContext = createBootstrapContext();
    ```
    4. set headless mode to on by configureHeadlessProperty method this tell spring u will note use a GUI enviroment
    5. loading system listners that will react to lifecycle events 
    ```java
    SpringApplicationRunListeners listeners =getRunListeners(args);
    ```
2. BUILD ENVIRONMENT + CONTEXT
    1. notifing the Listeners that the startup is beginning
    ```java
    listeners.starting(bootstrapContext, this.mainApplicationClass);
    ```
    2. parse environment args like --server.port=8080 and unify them into a structered object
    ```java
    ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
    ```
    3. build the environment by creating one unified object that holds :
        - System properties
        - env variables
        - application.yml
        - application.properties
        - profiles
    ```java
    ConfigurableEnvironment environment = prepareEnvironment(...)
    ```
    4. printing the spring banner pure cosmetic step nothing functional here
3. START SPRING CONTAINER (IoC)
    1. create APPLICATION CONTEXT and IoC objects IoC is usally refered to by an object named AnnotationConfigApplicationContext or ServletWebServerApplicationContext depending on the app type
    ```java
    context = createApplicationContext();
    ```
    2. attach a performance tracking system
    ```java 
    context.setApplicationStartup(this.applicationStartup);
    ```
    3. context preparing :
        - register beans
        - apply environment
        - register listeners
        - load configuration classes
    ```java
    prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
    ```
    4. context refresh the most important stage this the core of the springApplication this the real start of the spring container:
        - BeanFactory is initialized
        - beans are created
        - dependency injection happens
        - post-processors run
        - proxies created
        - embedded Tomcat starts (if web app)
    ```java
    refreshContext(context);
    ```
    5. after refresh
    ```java
    afterRefresh(context, applicationArguments);
    ``` 
    6. mark startup time
    ```java
    Duration timeTakenToStarted = startup.started();
    ``` 
    7. log startup info
    ```java
    StartupInfoLogger(...).logStarted(...);
    ```
    8. tell the listners that springApplication is now started
    ```java
    listeners.started(context, timeTakenToStarted);
    ``` 
4. FINALIZE + RUN USER CODE
    1. run your code by runnig:
    - CommandLineRunner
    - ApplicationRunner
    ```java
    callRunners(context, applicationArguments);
    ``` 
    2. ready event to tell the listners that springApplication is fully ready
    ```java
    listeners.ready(context, startup.ready());
    ```


