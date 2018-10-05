FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 8080

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]