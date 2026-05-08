# ApplicationContext

after the springApplication get intialized we call the run method wich does a lot of things but for sake of focusing on the topic we will only highlight some statements
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

## 1. Crete the application context
```java
	ConfigurableApplicationContext context = createApplicationContext();
    protected ConfigurableApplicationContext createApplicationContext() {
		ConfigurableApplicationContext context = this.applicationContextFactory
			.create(this.properties.getWebApplicationType());
		Assert.state(context != null, "ApplicationContextFactory created null context");
		return context;
	};
```
this method create the object ConfigurableApplicationContext but does NOT create beans applicationContextFactory create an instance depending on what Apptype ur spring app is if WebMVC u get AnnotationConfigServletWebServerApplicationContext, Reactive u get AnnotationConfigReactiveWebServerApplicationContext, Non-Web AnnotationConfigApplicationContext

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
1. set environment inside the context via the ConfigurableEnvironment wich is created by the springApplication
2. post process context
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
get the bean name generator,configure the resource loader, configure conversion services bean names should be unique so u dont conflict them , resource loader is a unified object that contains files classpaths templates confugration files static assets etc ..., conversion service convert configuration properties from strings to java fields with defined types
3. AOT optimize the scanning process  by pre-register beans to the bean factory in other words build a dependency graph
```java
addAotGeneratedInitializerIfNecessary(this.initializers);
```
4. excute initializers by looping on ApplicationContextInitializer and call the method initialize
```java
for (initializer : initializers) {
    initializer.initialize(context);
}
```
5. fire the context prepered event and notify the listners that the spring context is prepared
```java
listeners.contextPrepared(context);
```
6. closing the bootstrap temporary container because is no longer used
```java
bootstrapContext.close(context);
```
7. print startup logs
8. get the bean factory 
```java
ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
```
9. register internal singeltons to become manged beans
```java
beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
```
10. register banner bean
11. configure beanFactory circular behaviour so a bean A can depend on bean B and vice versa
12. allow bean override so to beans with the same name can override each other
13. lazy intialization so u can modify beans definitions before beans are instantiated
```java
context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
```
14. add an event listners that keeps the jvm alive in some scenarios
15. modify BeanFactory behavior before bean creation
```java
context.addBeanFactoryPostProcessor(new PropertySourceOrderingBeanFactoryPostProcessor(context));
```
16. load source classes 
- component scanning
- configuration parsing
- bean definition registration no bean instance yet

17. fire loaded event to notify the listners that the context has loaded bean definitions

## 3. refresh the application context

## 4. after refresh the application context

## 5. callRunners

## IoC
## Beans
### User beans
### Infrastructure beans