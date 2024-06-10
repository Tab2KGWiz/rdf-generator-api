package com.example.springboot;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@CrossOrigin(methods = {RequestMethod.POST})
public class RDFController {
    @PostMapping("/generateLinkedData")
    public ResponseEntity<?> generateLinkedData(@RequestParam("yamlFile") MultipartFile yamlFile,
                                                @RequestParam("csvFile") MultipartFile csvFile) throws IOException {
        byte[] linkedData;

        String url = "https://yarrrml-parser-rest-api-j7iai.ondigitalocean.app/yarrrml";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("yamlFile", yamlFile.getResource());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String url2 = "https://rmlmapper-webapi-js-677i7.ondigitalocean.app/execute";

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String rmlContent = response.getBody();
            String sourcesContent = new String(csvFile.getBytes());

            String[] lines = sourcesContent.split("\\r?\\n");
            StringJoiner joiner = new StringJoiner("\\n");

            for (String line : lines) {
                joiner.add(line);
            }

            String output = joiner.toString();

            String requestBody = String.format("{\"rml\":%s,\"sources\":{\"mappings.csv\":\"%s\"}}", rmlContent, output);

            HttpEntity<String> requestEntity2 = new HttpEntity<>(requestBody, headers2);

            ResponseEntity<String> responseEntity2 = restTemplate.exchange(url2, HttpMethod.POST, requestEntity2, String.class);

            linkedData = Objects.requireNonNull(Objects.requireNonNull(responseEntity2.getBody()).substring(11)).getBytes();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Content-Disposition", "attachment; filename=linked-data.txt")
                    .body(linkedData);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating YARRRML file.");
        }
    }
}



/*
curl -X POST "http://localhost:4000/execute" \
-H "accept: application/json" \
-H "Content-Type: application/json" \
-d '{
  "rml": "@prefix rr: <http://www.w3.org/ns/r2rml#> . @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . @prefix fnml: <http://semweb.mmlab.be/ns/fnml#> . @prefix fno: <https://w3id.org/function/ontology#> . @prefix d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#> . @prefix void: <http://rdfs.org/ns/void#> . @prefix dc: <http://purl.org/dc/terms/> . @prefix foaf: <http://xmlns.com/foaf/0.1/> . @prefix rml: <http://semweb.mmlab.be/ns/rml#> . @prefix ql: <http://semweb.mmlab.be/ns/ql#> . @prefix : <http://mapping.example.com/> . @prefix dbo: <http://dbpedia.org/ontology/> . @prefix schema: <http://schema.org/> . :rules_000 rdf:type void:Dataset ; void:exampleResource :map_CEP-2021-S1-WEIGHT.csv_000 . :map_CEP-2021-S1-WEIGHT.csv_000 rml:logicalSource :source_000 ; rdf:type rr:TriplesMap ; rdfs:label \"CEP-2021-S1-WEIGHT.csv\" ; rr:subjectMap :s_000 ; rr:predicateObjectMap :pom_000, :pom_001, :pom_002 . :source_000 rdf:type rml:LogicalSource ; rml:source \"mappings.csv\" ; rml:referenceFormulation ql:CSV . :s_000 rdf:type rr:SubjectMap ; rr:template \"http://dbpedia.org/ontology/{Weight}\" . :pom_000 rdf:type rr:PredicateObjectMap ; rr:predicateMap :pm_000 ; rr:objectMap :om_000 . :pm_000 rdf:type rr:PredicateMap ; rr:constant rdf:type . :om_000 rdf:type rr:ObjectMap ; rr:constant \"http://schema.org/Pork\" ; rr:termType rr:Literal . :pom_001 rdf:type rr:PredicateObjectMap ; rr:predicateMap :pm_001 ; rr:objectMap :om_001 . :pm_001 rdf:type rr:PredicateMap ; rr:constant dbo:animalid . :om_001 rdf:type rr:ObjectMap ; rml:reference \"Animal ID\" ; rr:termType rr:Literal ; rr:datatype <http://www.w3.org/2001/XMLSchema#integer> . :pom_002 rdf:type rr:PredicateObjectMap ; rr:predicateMap :pm_002 ; rr:objectMap :om_002 . :pm_002 rdf:type rr:PredicateMap ; rr:constant dbo:date . :om_002 rdf:type rr:ObjectMap ; rml:reference \"Date\" ; rr:termType rr:Literal ; rr:datatype <http://www.w3.org/2001/XMLSchema#date> .",
  "functionStateId": "1688549879688",
  "sources": {
    "mappings.csv": "Animal ID,Date,Weight\n982091062894196,2021-03-17,16300\n982091062894196,2021-03-18,15650\n982091062894196,2021-03-19,17800\n982091062894196,2021-03-20,16300\n982091062894196,2021-03-21,17700\n982091062894196,2021-03-22,17950\n982091062894196,2021-03-23,18200\n982091062894196,2021-03-24,18500\n982091062894196,2021-03-25,18900"
  }
}'

*/