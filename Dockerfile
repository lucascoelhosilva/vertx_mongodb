FROM openjdk:11-jdk-slim

ADD target/vertx_mongodb-1.0.0-SNAPSHOT-fat.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]
