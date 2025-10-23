FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY order-service/pom.xml ./order-service/
COPY discovery-server/pom.xml ./discovery-server/
COPY gateway/pom.xml ./gateway/
COPY user-service/pom.xml ./user-service/
RUN mvn dependency:go-offline -B
COPY order-service/src ./order-service/src/
RUN mvn package -pl order-service -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]