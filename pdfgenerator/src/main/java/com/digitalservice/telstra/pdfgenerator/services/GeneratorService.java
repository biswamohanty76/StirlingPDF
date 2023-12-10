package com.digitalservice.telstra.pdfgenerator.services;

import com.digitalservice.telstra.pdfgenerator.configs.StirlingPdfConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class GeneratorService implements IGeneratorService {
    private final StirlingPdfConfiguration stirlingPdfConfiguration;

    @Autowired
    private RestTemplate restTemplate;

    public GeneratorService(StirlingPdfConfiguration stirlingPdfConfiguration) {
        this.stirlingPdfConfiguration = stirlingPdfConfiguration;
    }

    public String getUrl() {
        String baseUrl = stirlingPdfConfiguration.getBaseUrl();
        System.out.println(baseUrl);
        return baseUrl;
    }

    @Override
    public ResponseEntity<ByteArrayResource> generate() throws IOException, URISyntaxException {
        String baseUrl = stirlingPdfConfiguration.getBaseUrl();
        Map<String,Object> endpointMap =  stirlingPdfConfiguration.getEndpoints();
        String generatePath = (String) endpointMap.get("generate");
        String requestUrl = baseUrl+ generatePath;

        URL url = getClass().getResource("/static/sample.html");
        url.openConnection();
        File f = new File(url.getFile());


        //File file = new File(url.toURI());
        Path path = Paths.get(url.toURI());
        return ResponseEntity.ok()
                .contentLength(f.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(Files.readAllBytes(path)));
    }
}
