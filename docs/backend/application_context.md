# ApplicationContext

After `SpringApplication` gets initialized, we call the `run` method, which does a lot of things. For the sake of focusing on the topic, we will only highlight some statements.

```java
public ConfigurableApplicationContext run(String... args) {
		...
		ConfigurableApplicationContext context = null;
		...
		try {
			...
			context = createApplicationContext();
			context.setApplicationStartup(this.applicationStartup);
			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			...
			callRunners(context, applicationArguments);
		}
		...
		return context;
	}
```

## 1. Create the application context

```java
	ConfigurableApplicationContext context = createApplicationContext();
    protected ConfigurableApplicationContext createApplicationContext() {
		ConfigurableApplicationContext context = this.applicationContextFactory
			.create(this.properties.getWebApplicationType());
		Assert.state(context != null, "ApplicationContextFactory created null context");
		return context;
	};
```

This method creates the `ConfigurableApplicationContext` object, but it does NOT create beans. `applicationContextFactory` creates an instance depending on what app type your Spring app is. If it is WebMVC, you get `AnnotationConfigServletWebServerApplicationContext`; if it is Reactive, you get `AnnotationConfigReactiveWebServerApplicationContext`; if it is non-web, you get `AnnotationConfigApplicationContext`.

## 2. Prepare the application context

```java
	private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
			ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments, @Nullable Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
		addAotGeneratedInitializerIfNecessary(this.initializers);
		applyInitializers(context);
		listeners.contextPrepared(context);
		bootstrapContext.close(context);
		if (this.properties.isLogStartupInfo()) {
			logStartupInfo(context);
			logStartupProfileInfo(context);
		}
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof AbstractAutowireCapableBeanFactory autowireCapableBeanFactory) {
			autowireCapableBeanFactory.setAllowCircularReferences(this.properties.isAllowCircularReferences());
			if (beanFactory instanceof DefaultListableBeanFactory listableBeanFactory) {
				listableBeanFactory.setAllowBeanDefinitionOverriding(this.properties.isAllowBeanDefinitionOverriding());
			}
		}
		if (this.properties.isLazyInitialization()) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		if (this.properties.isKeepAlive()) {
			context.addApplicationListener(new KeepAlive());
		}
		context.addBeanFactoryPostProcessor(new PropertySourceOrderingBeanFactoryPostProcessor(context));
		if (!AotDetector.useGeneratedArtifacts()) {
			// Load the sources
			Set<Object> sources = getAllSources();
			Assert.state(!ObjectUtils.isEmpty(sources), "No sources defined");
			load(context, sources.toArray(new Object[0]));
		}
		listeners.contextLoaded(context);
	}
```

1. Set the environment inside the context via the `ConfigurableEnvironment`, which is created by `SpringApplication`.
2. Post-process the context.

```java
protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
		if (this.beanNameGenerator != null) {
			context.getBeanFactory()
				.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			if (context instanceof GenericApplicationContext genericApplicationContext) {
				genericApplicationContext.setResourceLoader(this.resourceLoader);
			}
			if (context instanceof DefaultResourceLoader defaultResourceLoader) {
				defaultResourceLoader.setClassLoader(this.resourceLoader.getClassLoader());
			}
		}
		if (this.addConversionService) {
			context.getBeanFactory().setConversionService(context.getEnvironment().getConversionService());
		}
	}
```

Get the bean name generator, configure the resource loader, and configure conversion services. Bean names should be unique so you do not conflict them. Resource loader is a unified object that contains files, classpaths, templates, configuration files, static assets, etc. Conversion service converts configuration properties from strings to Java fields with defined types.

3. AOT optimizes the scanning process by pre-registering beans to the bean factory. In other words, it helps build a dependency graph.

```java
addAotGeneratedInitializerIfNecessary(this.initializers);
```

4. Execute initializers by looping over `ApplicationContextInitializer` and calling the `initialize` method.

```java
for (initializer : initializers) {
    initializer.initialize(context);
}
```

5. Fire the context prepared event and notify the listeners that the Spring context is prepared.

```java
listeners.contextPrepared(context);
```

6. Close the bootstrap temporary container because it is no longer used.

```java
bootstrapContext.close(context);
```

7. Print startup logs.
8. Get the bean factory.

```java
ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
```

9. Register internal singletons to become managed beans.

```java
beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
```

10. Register the banner bean.
11. Configure bean factory circular behavior so bean A can depend on bean B and vice versa.
12. Allow bean overriding so two beans with the same name can override each other.
13. Lazy initialization so you can modify bean definitions before beans are instantiated.

```java
context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
```

14. Add an event listener that keeps the JVM alive in some scenarios.
15. Modify BeanFactory behavior before bean creation.

```java
context.addBeanFactoryPostProcessor(new PropertySourceOrderingBeanFactoryPostProcessor(context));
```

16. Load source classes:

- component scanning
- configuration parsing
- bean definition registration, no bean instance yet

17. Fire the loaded event to notify the listeners that the context has loaded bean definitions.

## 3. Refresh the application context

```java
public void refresh() throws BeansException, IllegalStateException {
		this.startupShutdownLock.lock();
		try {
			this.startupShutdownThread = Thread.currentThread();

			StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

			// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);

				StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
				// Invoke factory processors registered as beans in the context.
				invokeBeanFactoryPostProcessors(beanFactory);
				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);
				beanPostProcess.end();

				// Initialize message source for this context.
				initMessageSource();

				// Initialize event multicaster for this context.
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}
			catch (RuntimeException | Error ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Stop already started Lifecycle beans to avoid dangling resources.
				if (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning()) {
					try {
						this.lifecycleProcessor.stop();
					}
					catch (Throwable ex2) {
						logger.warn("Exception thrown from LifecycleProcessor on cancelled refresh", ex2);
					}
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}
			finally {
				contextRefresh.end();
			}
		}
		finally {
			this.startupShutdownThread = null;
			this.startupShutdownLock.unlock();
		}
}
```

1. Prepare context state.

```java
prepareRefresh();
```

- marks context as “active startup”
- initializes startup timestamps
- validates environment
- prepares internal flags

2. Prepare BeanFactory.

```java
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
prepareBeanFactory(beanFactory);
```

Create or refresh the bean factory, load bean definitions, and set up the container created earlier in the `prepareContext` phase. Then inject environment, system beans, classloader rules, resolution shortcuts, and infrastructure dependencies.

3. Modify bean definitions.

```java
invokeBeanFactoryPostProcessors(beanFactory);
```

Well, as we proceed to know, Spring uses reflection to allow runtime resolution. If `SpringApplication` scanned components, defined beans, and then created them immediately, Spring would be a static DI container. But Spring scans and allows bean modification before instantiating objects, so Spring can register auto configuration. Dependencies can also register new beans or modify existing ones. So what does this do:

- runs `@Configuration` processing
- processes `@ComponentScan`
- processes `@Bean`
- modifies / adds `BeanDefinitions`

4. Register bean processors.

```java
registerBeanPostProcessors(beanFactory);
```

After the bean is created, Spring registers a post processor for every bean so later, when a bean is created, those post processors run before returning the bean to the `ApplicationContext`. For `@Autowired`, we have `AutowiredAnnotationBeanPostProcessor`, and this processor scans fields, finds `@Autowired`, and injects dependencies. Also, AOP has `AnnotationAwareAspectJAutoProxyCreator`, which creates proxies for beans, so instead of calling a raw bean, you call through a proxy. The proxy executes the logic that wraps the bean first, then proceeds to execute the bean. Spring also registers proxy creators, which decide whether a bean should or should not be wrapped with a proxy.

5. Set up infrastructure.

```java
initMessageSource();
initApplicationEventMulticaster();
onRefresh();
registerListeners();
```

- message system (i18n)
- event system
- lifecycle hooks `start()`, `stop()`, `isRunning()`
- web server startup (in web apps)

6. Create singleton beans.

```java
finishBeanFactoryInitialization(beanFactory);
```

- all singleton beans are instantiated
- dependencies injected
- proxies created
- `@Autowired` resolved
- `@PostConstruct` executed

So basically, Spring creates the beans, populates dependencies by injecting repositories, services, etc., applies `BeanPostProcessors` before bean initialization, then initializes beans, then applies `BeanPostProcessors` again. This is where proxies are often created, and finally stores the final bean in the bean map: `singletonObjects.put(beanName, finalBean)`.

7. Finish startup.

```java
finishRefresh();
```

- publish `ContextRefreshedEvent`
- start lifecycle beans (beans that implement `SmartLifecycle`)
- mark the context as fully running: `context.setActive(true);`

## 4. callRunners

Runners are beans that implement either `CommandLineRunner` or `ApplicationRunner`, and their utility is to run customized code automatically right after Spring Boot finishes starting, like data preload, database connection checks, printing debug info, etc. You can decide the order of execution with `@Order`.
