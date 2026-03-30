#docker build -t customers-service .
FROM eclipse-temurin:21.0.5_11-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs ./libs
COPY build/resources ./resources
COPY build/libs/customers-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]