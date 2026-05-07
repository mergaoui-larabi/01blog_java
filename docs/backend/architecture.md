# architecture

## folder structure

This is the tree of the backend spring API:

```tree
backend/
│
├── api-gateway/              # Entry point (Spring Cloud Gateway)
├── user-service/
├── post-service/
├── notification-service/
├── report-service/
│
├── common/                  # Shared code (DTOs, utils)
│
└── pom.xml (parent)
```

This is the tree of one of the services:


```tree
user-service/
│
├── src/
│   └── main/
│       ├── java/com/app/user/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   ├── dto/
│       │   └── config/
│       │
│       └── resources/
│           ├── application.yml
│           └── db/
│               └── migration/   👈 (IMPORTANT)
│                   ├── V1__init.sql
│                   ├── V2__add_roles.sql
│
├── Dockerfile
└── pom.xml
```

## Spring Initializr

```sh
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d dependencies=web,data-jpa,postgresql,flyway \
  -d name=name-service \
  -o user-service.zip
```


