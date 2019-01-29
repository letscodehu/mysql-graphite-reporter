FROM maven:3.6.0-jdk-8-slim

COPY . /opt/reporter
WORKDIR /opt/reporter
RUN mvn clean package
RUN cp target/sql-graphite-reporter-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "file.conf"]