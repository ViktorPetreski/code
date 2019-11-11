FROM openjdk:11.0.4-jre-slim

RUN mkdir /app

WORKDIR /app

ADD ./api/target/exercise-module-api-1.0-SNAPSHOT.jar /app

EXPOSE 8080

CMD ["java", "-jar", "exercise-module-api-1.0-SNAPSHOT.jar"]
