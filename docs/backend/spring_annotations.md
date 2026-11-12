# Spring Annotations

## Bean

a bean is an object registred to the spring IoC container as a result of component scaning so the spring manage its lifecycle and can inject it in to other beans

### bean scops

1. singleton: One instance per bean definition per Spring IoC container.
2. prototype: A new instance each time Spring is asked to create that bean.
3. request: One instance per HTTP request.
4. session: One instance per HTTP session.
5. application: One instance per ServletContext.
6. websocket: One instance per WebSocket session.

```java
@Service
@Scope("singleton")
public class JwtService {
}
```

### bean declarition

```java
@Configuration
public class JwtConfig {

    @Bean
    public JwtService jwtService(String secret) {
        return new JwtService(secret);
    }
}
```

### bean injection

```java
@Service
public class AuthenticationService {

    private final JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }
}
```

here specifing the JwtService type in the constructor tell spring that this bean candidate @Service requires a bean with type JwtService.class
so spring look for the beans with class type JwtService.class and to avoid ambiguity he users @primary or @qualifier

## Proxy

the proxies are created in 2 ways first using jdk dynamic proxy or cglib wich creates an object that implemnts/extends the targeted bean and creat a proxy wich responsible of Executing additional behavior around a method invocation

## ConfigurationPropertiesScan

this annotation tell spring to scan packages using reflect api and look for classes annotatedwith @ConfigurationProperties and register them as beans

## Component

this annotation tell spring that this class is a candidate that might containes beans to be registred to the IoC container via component scanning

### Configuration

this annotations is meta-annotated with @Component , its role is to add enhancement to the configuration class using CGLIB so for instance

```java
@Configuration
public class SecurityConfig {

    @Bean
    public A beanA() {
        System.out.println("Creating PasswordEncoder");
        return new A();
    }

    @Bean
    public B beanB() {
        return new B(beanA());
    }
}
```

so in this example the when we create bean B we call the method beanA() but instead of creating another object we use the bean A registred earlier

### RestController

this annotations is meta-annotated with @ResponseBody @Controller(@Component)

```txt
Spring Boot Application
    |
    v
ApplicationContext
    |
    | Contains Spring beans
    |
    +---- DispatcherServlet
    |
    +---- RestController
    |
    +---- Service
    |
    +---- Repository
    |
    v
ServletRegistrationBean
    |
    | Registers DispatcherServlet
    v
Tomcat Servlet Container
    |
    | Receives HTTP requests
    v
DispatcherServlet
    |
    | Finds the matching handler
    v
RestController
    |
    v
Service
    |
    v
Repository
```

### RestControllerAdvice

this annotations is used to centrelize controllers error handling instead of spaming try/catch block in each controller u define the behaiviour u want for each thrown exception and handle them all at once

```txt
HTTP Request
     |
     v
RestController
     |
     v
Service
     |
     v
NotFoundException thrown
     |
     v
Spring MVC exception resolution
     |
     v
@RestControllerAdvice
     |
     v
@ExceptionHandler(NotFoundException.class)
     |
     v
HTTP 404 Response
```

### Repository

this is a spring streotype annotations wich its main role is to add exception translation so it transtale hibernate/jdbc/jpa exceptions to DataAccessExceptions
this allows service layer to handle the persistence errors without depending directly on a particular persistence implementation

### Service

this is an annotation is just specificatino of the @Component, from Spring's perspective, both are component-scanned and registered as singleton beans by default.
