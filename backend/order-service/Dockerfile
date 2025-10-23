FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY backend/pom.xml .
COPY backend/order-service/pom.xml ./order-service/
COPY backend/discovery-server/pom.xml ./discovery-server/
COPY backend/gateway/pom.xml ./gateway/
COPY backend/user-service/pom.xml ./user-service/
RUN mvn dependency:go-offline -B
COPY backend/order-service/src ./order-service/src/
RUN mvn package -pl order-service -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/order-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]