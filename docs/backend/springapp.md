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

## SpringApplication


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

these are java.lang.annotation now lets explore org.springframework.context.annotation :

- @AnnotationBeanNameGenerator;
- @Bean;
- @ComponentScan;
- @ComponentScan.Filter;
- @Configuration;
- @FilterType;
- @AliasFor;

