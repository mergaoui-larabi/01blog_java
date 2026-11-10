# building the jar
FROM maven:3.10.0-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src src

# Debuging
RUN ls -la

# build the jar after bringing the necessary files
RUN ./mvnw clean package -DskipTests

#running the jar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5001

ENTRYPOINT ["java", "-jar", "app.jar"]
