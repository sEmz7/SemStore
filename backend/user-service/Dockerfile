FROM openjdk:21-jdk
COPY target/*.jar /app/user-service.jar
WORKDIR /app
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar user-service.jar"]