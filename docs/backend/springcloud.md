# Spring Cloud

## Why?
we added the spring cloud gateway to be able to create filters forward request and route matching
## Reactor Netty vs Tomcat 
Tomcat uses thread-per-request model:
- Receives request
- Calls database
- WAITS
- Gets response
- Sends response

Reactor Netty uses Event-loop model:

```java
@GetMapping("/users")
public Mono<User> user(){

    return userService.findUser();
}
```

mono means i give you the result later it like a promesse

## 