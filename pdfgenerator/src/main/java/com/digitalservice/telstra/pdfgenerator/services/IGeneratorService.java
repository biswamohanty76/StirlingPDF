package com.digitalservice.telstra.pdfgenerator.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

public interface IGeneratorService {
    ResponseEntity<ByteArrayResource> generate() throws IOException, URISyntaxException;
}
