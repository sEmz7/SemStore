FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . /app/
RUN mvn clean package -pl order-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]