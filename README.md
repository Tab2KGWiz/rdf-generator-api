# Spring Boot RDF GENERATOR API service

This is a Spring Boot application that generates RDF data from a given CSV and Yaml file. The application uses the [RML.io](https://rml.io/) library's to generate the RDF data.

## How to Run

You run it using the `java -jar` command.

- Clone this repository
- Make sure you are using JDK 17 and Maven 3.x
- You can build the project and run the tests by running `mvn clean package`
- Once successfully built, you can run the service by one of these two methods:

```
        java -jar target/spring-boot-0.0.1-SNAPSHOT.jar
or
        mvn spring-boot:run
```

Once the application runs you should see something like this

```
2024-07-02T12:05:43.529+02:00  INFO 91552 --- [spring-boot] [           main] com.example.springboot.Application       : No active profile set, falling back to 1 default profile: "default"
2024-07-02T12:05:44.967+02:00  INFO 91552 --- [spring-boot] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8081 (http)
```

## To consume the API

    $ curl -X POST -F "yamlFile=@mapping.yml" -F "csvFile=@sample.csv" http://localhost:8081/generateLinkedData > sample.nt

## Docker

### Install

    $ docker build -t rdf-generator-api .

### Run

    $ docker run -p 8081:8081 -it rdf-generator-api

### Docker hub

    $ docker pull zihancr/rdfgenerator
