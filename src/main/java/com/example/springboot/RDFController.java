package com.example.springboot;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
@CrossOrigin(methods = {RequestMethod.POST})
public class RDFController {
    @PostMapping("/generateLinkedData")
    public ResponseEntity<?> generateLinkedData(@RequestParam("yamlFile") MultipartFile yamlFile,
                                                @RequestParam("csvFile") MultipartFile csvFile) throws IOException {
        byte[] linkedData = new byte[0];

        String staticPath = "/root/resources";

        File csvData = new File(staticPath, "mappings.csv");

        File yamlData = new File(staticPath, "mappings.yarrrml.yml");
        File outputData = new File(staticPath, "output.txt");
        File rulesFile = new File(staticPath, "rules.rml.ttl");

        yamlFile.transferTo(yamlData);
        csvFile.transferTo(csvData);

        ProcessBuilder builder1 = new ProcessBuilder("docker", "run", "--rm", "-v", staticPath + ":/data",
                "rmlio/yarrrml-parser:latest", "-i", "/data/mappings.yarrrml.yml");

        builder1.redirectOutput(rulesFile);

        try {
            Process process = builder1.start();
            process.waitFor(); // Wait for the container to finish
            int exitCode = process.exitValue();

            if (exitCode == 0) {
                ProcessBuilder builder2 = new ProcessBuilder("docker", "run", "--rm", "-v",
                        staticPath + ":/data", "rmlio/rmlmapper-java", "-m", "rules.rml.ttl");

                try {
                    Process process2 = builder2.start();

                    int exitCode2 = process.exitValue();

                    if (exitCode2 == 0) {
                        System.out.println("RML mapper run successfully!");
                        InputStream stream = process2.getInputStream();

                        linkedData = stream.readAllBytes();
                    } else {
                        System.err.println("Error running RML mapper. Exit code: " + exitCode2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Yarrrml parser run successfully!");
            } else {
                throw new RuntimeException("Error running Yarrrml parser. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=linked-data.txt")
                .body(linkedData);
    }
}