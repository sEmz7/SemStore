FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY discovery-server/pom.xml ./discovery-server/
COPY gateway/pom.xml ./gateway/
COPY user-service/pom.xml ./user-service/
COPY order-service/pom.xml ./order-service/
RUN mvn dependency:go-offline -B
COPY discovery-server/src ./discovery-server/src/
RUN mvn package -pl discovery-server -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/discovery-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]