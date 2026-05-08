# SpringBootApplication

## Introduction

After initializing a Spring app, you get `src/main/java/com/example/demo`.
In this path, you find a file named after the application. Let us call it `App.java`.
Inside, you see a class named `App` that contains the `main` method, which is your entry point to the Spring Boot application.
This class calls `SpringApplication`.
Above the class declaration, you find an annotation: `@SpringBootApplication`.

## Execution

1. Your Java code (`.java`)
2. Maven compiles to (`.class`)
3. `app.jar` (bytecode + `application.properties` / `application.yml` + static files)
4. JVM starts (runs Spring bytecode + your bytecode)
5. Spring runs:
   - scans annotations (via reflection)
   - creates objects (beans)
   - wires dependencies

## Reflection

The JVM exposes metadata for every loaded class, such as:

- class name
- methods
- fields
- constructor
- annotations

Spring uses reflection by scanning classes on the classpath (including `target/classes/`).

- load the class
- check for annotations
- create objects dynamically
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

- `@Documented`
  - Any class using that annotation will show it in generated documentation.

- `@Inherited`
  - Allows subclasses to inherit parent class annotations without explicitly retyping them.

- `@Retention(RetentionPolicy.RUNTIME)`
  - Decides the scope of annotation retention: `SOURCE` / `CLASS` / `RUNTIME`.
  - `SOURCE`: discarded after compilation.
  - `CLASS`: stored in `.class` metadata but not available through runtime reflection (default behavior).
  - `RUNTIME`: stored in `.class` metadata and available at runtime via reflection.

- `@Target(ElementType.TYPE)`
  - Indicates where the annotation is applicable. `TYPE` allows class, interface, record, enum.

These are from `java.lang.annotation`. Now let us explore `org.springframework.context.annotation`:

- `@AnnotationBeanNameGenerator`
- `@Bean`
- `@ComponentScan`
- `@ComponentScan.Filter`
- `@Configuration`
- `@FilterType`
- `@AliasFor`

## SpringApplication

## Chronological Order

- JVM loads your `main` method from the app class.

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

- Then it runs `SpringApplication.run(...)`. This method creates an instance of `SpringApplication` internally by passing `TestServiceApplication.class`.
- `.class` is an object that defines the class itself. Think of it as class metadata that holds class name, fields, methods, annotations, etc.
- The class metadata passed to `SpringApplication` is called `primarySources`. Note that Spring accepts multiple source classes.
- The instance is created in this manner:

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

So let us walk through the 5 steps to initialize a `SpringApplication`:

1. Verify that primary sources are not null, because Spring needs at least one source class. Then store them in a set because Spring avoids duplicated configuration.
2. Discover the app type: `SERVLET` / `REACTIVE` / `NONE`. `SERVLET` means classic web app (Tomcat/MVC), `REACTIVE` means reactive web app (WebFlux), `NONE` means CLI or non-web app.
3. When you run the Spring Boot app, your JVM may have JARs like:
   - `your-app.jar`
   - `spring-boot.jar`
   - `spring-context.jar`
   - `spring-core.jar`
   - etc.

The JVM can load classes from all of them. Besides that, your JAR contains resource files. As discussed earlier, these resources may include class names that implement types such as `BootstrapRegistryInitializer`, `ApplicationContextInitializer`, and `ApplicationListener`.

So Spring loads those class paths. They may refer to dependency classes or your own classes, but most of the time they come from dependencies or third-party libraries. After JVM loading, objects are instantiated using reflection and returned as lists.

```java
Class<?> dynClass = Class.forName("com.example.dynClass");
Object obj = dynClass.getConstructor().newInstance();
```

4. Store the main application class by crawling stack frames. At that moment, the call stack is like this:
   - `mainApplication.main()`
   - `-> SpringApplication.run()`
   - `-> constructor`
   - `-> deduceMainApplicationClass()`

The top of the stack contains the currently executed stack frame, so it walks to locate the main class.

- Note: we initialize 3 types of lists:
1. `bootstrapRegistryInitializers`: classes in JARs that implement `BootstrapRegistryInitializer`. They are for ultra-early setup, before application context initialization.
2. `applicationContextInitializer`: classes in JARs that implement `ApplicationContextInitializer`. These run before context refresh, to customize the context.
3. `applicationListener`: classes in JARs that implement `ApplicationListener`. These react to lifecycle events. Spring can emit events such as `ApplicationStartingEvent` (startup beginning), `ApplicationEnvironmentPreparedEvent` (environment ready), `ApplicationPreparedEvent` (context prepared), `ApplicationStartedEvent` (startup finished).

Now the Spring instance is initialized. Let us go to the next phase, which is the `run` method:

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

- This code has 4 major phases:

1. PREPARE STARTUP INFRASTRUCTURE
   1. Create a timer that tracks how long startup takes. Nothing functional yet.
   ```java
   Startup startup = Startup.create();
   ```
   2. Create a JVM shutdown hook. This is crucial for safety because when JVM stops, SpringApplication can shut down gracefully.
   ```java
   if (this.properties.isRegisterShutdownHook()) {
       SpringApplication.shutdownHook.enableShutdownHookAddition();
   }
   ```
   3. Bootstrap Context: temporary container that holds early startup objects before creating the application context.
   ```java
   DefaultBootstrapContext bootstrapContext = createBootstrapContext();
   ```
   4. Set headless mode by `configureHeadlessProperty()`. This tells Spring you are not using a GUI environment.
   5. Load system listeners that react to lifecycle events.
   ```java
   SpringApplicationRunListeners listeners = getRunListeners(args);
   ```

2. BUILD ENVIRONMENT + CONTEXT
   1. Notify listeners that startup is beginning.
   ```java
   listeners.starting(bootstrapContext, this.mainApplicationClass);
   ```
   2. Parse environment args like `--server.port=8080` and unify them into a structured object.
   ```java
   ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
   ```
   3. Build the environment by creating one unified object that holds:
      - System properties
      - env variables
      - `application.yml`
      - `application.properties`
      - profiles
   ```java
   ConfigurableEnvironment environment = prepareEnvironment(...)
   ```
   4. Print the Spring banner. Pure cosmetic step, nothing functional.

3. START SPRING CONTAINER (IoC)
   1. Create APPLICATION CONTEXT and IoC objects. IoC is usually represented by objects like `AnnotationConfigApplicationContext` or `ServletWebServerApplicationContext` depending on app type.
   ```java
   context = createApplicationContext();
   ```
   2. Attach a performance tracking system.
   ```java
   context.setApplicationStartup(this.applicationStartup);
   ```
   3. Context preparation:
      - register beans
      - apply environment
      - register listeners
      - load configuration classes
   ```java
   prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
   ```
   4. Context refresh: the most important stage, the core of SpringApplication, and the real start of the Spring container:
      - BeanFactory is initialized
      - beans are created
      - dependency injection happens
      - post-processors run
      - proxies are created
      - embedded Tomcat starts (if web app)
   ```java
   refreshContext(context);
   ```
   5. After refresh:
   ```java
   afterRefresh(context, applicationArguments);
   ```
   6. Mark startup time:
   ```java
   Duration timeTakenToStarted = startup.started();
   ```
   7. Log startup info:
   ```java
   StartupInfoLogger(...).logStarted(...);
   ```
   8. Tell listeners that SpringApplication has started:
   ```java
   listeners.started(context, timeTakenToStarted);
   ```

4. FINALIZE + RUN USER CODE
   1. Run your code by running:
      - `CommandLineRunner`
      - `ApplicationRunner`
   ```java
   callRunners(context, applicationArguments);
   ```
   2. Ready event tells listeners that SpringApplication is fully ready:
   ```java
   listeners.ready(context, startup.ready());
   ```
