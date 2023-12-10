package com.digitalservice.telstra.pdfgenerator.controllers;

import com.digitalservice.telstra.pdfgenerator.configs.StirlingPdfConfiguration;
import com.digitalservice.telstra.pdfgenerator.services.GeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GeneratorController {
    private final GeneratorService generatorService;

    private final StirlingPdfConfiguration stirlingPdfConfiguration;

//    @Autowired
//    private RestTemplate restTemplate;

    @Autowired
    HttpServletResponse response;

    public GeneratorController(GeneratorService generatorService, StirlingPdfConfiguration stirlingPdfConfiguration) {
        this.generatorService = generatorService;
        this.stirlingPdfConfiguration = stirlingPdfConfiguration;
    }

    @GetMapping("/baseurl")
    public ResponseEntity getStirlingUrl() {

        return ResponseEntity.ok(generatorService.getUrl());

    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate() throws IOException, URISyntaxException, ClassNotFoundException {
        String baseUrl = stirlingPdfConfiguration.getBaseUrl();
        Map<String, Object> endpointMap = stirlingPdfConfiguration.getEndpoints();
        String generatePath = (String) endpointMap.get("generate");
        String requestUrl = baseUrl + generatePath;

        URL url = getClass().getResource("/static/sample.html");
        url.openConnection();
        File f = new File(url.getFile());


        URL destUrl = getClass().getResource("/output");
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(
                        new SimpleClientHttpRequestFactory()
                )
        );
        byte[] result = null;
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("fileInput", new FileSystemResource(f));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(params, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                builder.build().encode().toUri(),
                HttpMethod.POST,
                requestEntity,
                String.class);

        String fileName = getFileName(responseEntity.getHeaders());
        fileName = URLDecoder.decode(fileName, StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_PDF_VALUE))
                .contentLength(responseEntity.getHeaders().getContentLength()) //
                .body(responseEntity.getBody());

    }

    private String getFileName(HttpHeaders httpHeaders) {
        String disposition = httpHeaders.get("Content-Disposition").toString();
        String fileName = disposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
        return fileName;
    }

    @PostMapping(value = "/generateSPDF" , consumes = "multipart/form-data")
    public ResponseEntity<?> convertBySPDF(@RequestPart MultipartFile multipartFile, @RequestParam String path) throws IOException {
        String baseUrl = stirlingPdfConfiguration.getBaseUrl();
        Map<String, Object> endpointMap = stirlingPdfConfiguration.getEndpoints();
        String generatePath = (String) endpointMap.get("generate");
        String requestUrl = baseUrl + generatePath;

        InputStream inputStream = multipartFile.getInputStream();
        byte[] data =inputStream.readAllBytes();

        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("fileInput", new FileSystemResource((File) multipartFile));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(
                        new SimpleClientHttpRequestFactory()
                )
        );
        ResponseEntity responseEntity = restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity , String.class);
        return responseEntity;
    }
}
