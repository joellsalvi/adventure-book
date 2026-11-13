package com.pictet.adventurebookapi.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsPropertiesTest {

    @Test
    void when_nothing_is_configured__then_no_origin_is_allowed_and_defaults_apply() {
        // WHEN
        final CorsProperties properties = new CorsProperties(null, null, null);

        // THEN
        assertTrue(properties.allowedOrigins().isEmpty());
        assertEquals(List.of("GET"), properties.allowedMethods());
        assertEquals(3600L, properties.maxAge());
    }

    @Test
    void when_values_are_configured__then_they_are_kept_as_an_immutable_copy() {
        // GIVEN
        final List<String> origins = new ArrayList<>(List.of("http://localhost:4200"));

        // WHEN
        final CorsProperties properties = new CorsProperties(origins, List.of("GET", "POST"), 60L);
        origins.add("http://other.example.com");

        // THEN
        assertEquals(List.of("http://localhost:4200"), properties.allowedOrigins());
        assertEquals(List.of("GET", "POST"), properties.allowedMethods());
        assertEquals(60L, properties.maxAge());
    }
}
