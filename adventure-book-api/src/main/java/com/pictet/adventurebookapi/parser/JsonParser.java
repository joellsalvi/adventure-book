package com.pictet.adventurebookapi.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class JsonParser {

    private static final String BOOKS_ROOT_PATH = "books/";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T parseJson(final String fileName, Class<T> type) throws IOException {

        final ClassPathResource resource = new ClassPathResource(BOOKS_ROOT_PATH + fileName);
        if (!resource.exists()) {
            throw new FileNotFoundException("Book file not found on classpath: " + BOOKS_ROOT_PATH + fileName);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, type);
        }
    }
}
