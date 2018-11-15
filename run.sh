#! /bin/bash

./mvnw package
if [[ "$?" -ne 0 ]] ; then
    echo ""
    echo "      +================================================================+";
    echo "      |                   COULD NOT COMPILE PROJECT                    |";
    echo "      +================================================================+";
    echo ""
    exit 1;

fi
docker build -t credit-ocr --build-arg JAR_FILE=./target/credit-ocr-0.0.1-SNAPSHOT.jar .
docker-compose up
