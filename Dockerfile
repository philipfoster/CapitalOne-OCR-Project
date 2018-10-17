FROM  ubuntu:18.04
VOLUME /tmp
EXPOSE 8080
RUN apt-get update && \
    apt-get install -y wget libtesseract-dev tesseract-ocr ghostscript

RUN wget https://download.java.net/java/GA/jdk11/28/GPL/openjdk-11+28_linux-x64_bin.tar.gz -O /tmp/openjdk-11+28_linux-x64_bin.tar.gz && \
    tar -xzvf /tmp/openjdk-11+28_linux-x64_bin.tar.gz && \
    rm -f /tmp/openjdk-11+28_linux-x64_bin.tar.gz && \
    echo This better work


ENV JAVA_HOME /tmp/jdk-11

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["/jdk-11/bin/java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]