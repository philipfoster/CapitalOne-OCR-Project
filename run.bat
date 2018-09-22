@echo off
REM This script will compile the project, build the docker image, and launch it with all necessary dependencies

call mvnw package
REM docker-compose build
docker build -t credit-ocr .
docker-compose up