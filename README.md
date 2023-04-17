# Project Name
**Drone Service API**

## Description

This is a Spring Boot project that aims to build a RESTFul API for an DroneService. The API allows customers to browse
and purchase products, manage their shopping cart, and check out securely. The project is built using Spring Boot
framework, with Spring MVC for handling HTTP requests, Spring Data JPA for data persistence, It also utilizes H2 in-memory database for storing product and customer data during
development and testing.

## Features

1. User-friendly RESTful API for managing Drones.
2. Support for handling various HTTP methods and error handling.
3. Integration with H2 in-memory database for development and testing.
4. Automated unit and integration testing using JUnit and Mockito.
5. using docker
6. using lombok
7. using swagger
##  the design approach.
* registerdrone (/api/v1/drones/register)  : just send DTO to create new Drone, but you can't send same serial no
* loaddrone(/api/v1/drones/load) : drone must have IDLE or LOADING state then you can load some medications to drone and if drone is IDLE will automatic will change to LOADING(my assumption)
* changeStateOfDone(/api/v1/drones/{serialNo}/{state}) : you can change state of drone but by sequence like LOADING to LOADED and if you changed To IDLE will automatic remove all medication from the drone(my assumption)
* checkMedicationsForGivenDrone(/api/v1/drones/{serialNo}/medications) : just return all medications for a given drone
* availableDronesForLoading(/api/v1/drones/availabilities) : return all drone with IDLE AND LOADING only (my assumption)
* batteryLevelOfDrone(/api/v1/drones/batteries/{serialNo}) : return battery percentage of drone if found
* you can check file event log in the root of the project name= battery_log.txt


## required
1. java 17
2. spring boot 3
3. docker
4. maven
## Installation
_at path of the project_ 
1. for maven
`mvn clean install
   `
2. for docker
 

     ` docker pull eclipse-temurin:17-jdk-jammy`
    
    
     `docker build -t drone-service-api:1.0 .`
     
     
     `docker run -d -p 8080:8080 -t drone-service-api:1.0`

you can change tha port but don't forget to change it also in post man

3. import drone-service-api.json to your postman you will found in the root


## Contact Information
For questions, suggestions, or bug reports, please contact [fady.raafatfarouk@gmail.com] or open an issue on the GitHub repository
