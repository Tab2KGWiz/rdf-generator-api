package com.example.springboot;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(methods = { RequestMethod.POST, RequestMethod.GET })
public class RDFController {

    private final String yarrrmlService = System.getenv("YARRRML_SERVICE") != null ? System.getenv("YARRRML_SERVICE")
            : "yarrrml";
    private final String rmlmapperService = System.getenv("RMLMAPPER_SERVICE") != null
            ? System.getenv("RMLMAPPER_SERVICE")
            : "rmlmapper";

    @PostMapping("/generateLinkedData")
    public ResponseEntity<?> generateLinkedData(@RequestParam("yamlFile") MultipartFile yamlFile,
            @RequestParam("csvFile") MultipartFile csvFile) throws IOException {
        String yarrrmlUrl = String.format("http://%s:3000/yarrrml", yarrrmlService);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("yamlFile", yamlFile.getResource());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(yarrrmlUrl, HttpMethod.POST, requestEntity,
                String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String rmlUrl = String.format("http://%s:4000/execute", rmlmapperService);

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

            String requestBody = String.format("{\"rml\":%s,\"sources\":{\"mappings.csv\":\"%s\"}}", rmlContent,
                    output);

            HttpEntity<String> requestEntity2 = new HttpEntity<>(requestBody, headers2);

            ResponseEntity<String> responseEntity2 = restTemplate.exchange(rmlUrl, HttpMethod.POST, requestEntity2,
                    String.class);

            String jsonString = responseEntity2.getBody();

            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.getFactory()
                        .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(100_000_000).build());
                JsonNode jsonNode = mapper.readTree(jsonString);
                jsonString = jsonNode.get("output").asText();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Content-Disposition", "attachment; filename=linked-data.txt")
                    .body(jsonString);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating YARRRML file.");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}