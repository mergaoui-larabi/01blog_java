# request_response 

## big picture
```text
Client
 ↓ HTTPS
Nginx
 ↓
Spring Gateway (Reactor Netty) webflux
 ↓
Some Service (Tomcat)
 ↓
DispatcherServlet
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

