FROM openjdk:21-slim
WORKDIR /app
COPY build/libs/kafka-consumer-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]