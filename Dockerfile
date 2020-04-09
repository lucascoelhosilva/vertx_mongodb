FROM sensedia/openjdk11-base:latest

ADD target/vertx_mongodb-1.0.0-SNAPSHOT-fat.jar /sensedia/app.jar

CMD /usr/bin/java $JAVA_OPTS -jar app.jar
