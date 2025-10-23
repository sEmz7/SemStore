FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY user-service/pom.xml ./user-service/
COPY discovery-server/pom.xml ./discovery-server/
COPY gateway/pom.xml ./gateway/
COPY order-service/pom.xml ./order-service/
RUN mvn dependency:go-offline -B
COPY user-service/src ./user-service/src/
RUN mvn package -pl user-service -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/user-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]