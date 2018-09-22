# Capital One OCR Project

Capital One receives thousands requests via mail every month from customers requesting to change data listed on their credit report. 
Presently, employees must scan these letters into an electronic database, however they must still manually read these letters to extract
important information such as the customer’s name, address, SSN, account numbers, etc.  The proposed system will ingest scanned letters
and process them using OCR and Natural Language Processing technology to extract relevant customer details and add them to the database,
as well as attempt to detect if the request is frivolous. This will save Capital One employees time and increase the number of letters
Capital One can process without needing to hire additional staff.


## Running the project
This project runs in a Docker container. To build and run, execute the following commands
CD to the project root
`mvnw package`
`docker build -t credit-ocr .`
`docker-compose up`

Alternatively, you may just execute the `run.bat` or `run.sh` script included in the project to automate the above process.
 

## Modifying the project
The project is primarily configured with environment variables passed to the docker image. This can be done 
through the `environment` section of the docker-compose file.

- `spring.datasource.username`: The postgres username for the application
- `spring.datasource.password`: The password for the user specified in `spring.datasource.username`
- `spring.datasource.url`: The JDBC URL for the postgres database instance. 

