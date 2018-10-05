#! /bin/bash

./mvnw package
docker build -t credit-ocr --build-arg JAR_FILE=./target/credit-ocr-0.0.1-SNAPSHOT.jar .
docker-compose up