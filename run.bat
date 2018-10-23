@echo off
REM This script will compile the project, build the docker image, and launch it with all necessary dependencies

call mvnw package
if %errorlevel% NEQ 0 {
    echo.
    echo.
    echo                              COULD NOT COMPILE PROJECT
    echo.
    echo.
    exit /b %errorlevel%
}
docker build -t credit-ocr --build-arg JAR_FILE=./target/credit-ocr-0.0.1-SNAPSHOT.jar .
docker-compose up