FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY backend/pom.xml .
COPY backend/gateway/pom.xml ./gateway/
COPY backend/discovery-server/pom.xml ./discovery-server/
COPY backend/user-service/pom.xml ./user-service/
COPY backend/order-service/pom.xml ./order-service/
COPY backend/common-dtos/pom.xml ./common-dtos/
RUN mvn dependency:go-offline -B
COPY backend/gateway/src ./gateway/src/
RUN mvn package -pl gateway -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]