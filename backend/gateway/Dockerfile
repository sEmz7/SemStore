FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . /app/
RUN mvn clean package -pl gateway -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]