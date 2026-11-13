package com.pictet.adventurebookapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

/**
 * CORS settings, bound from the {@code cors.*} properties in application.yaml.
 * Can be overridden per environment, e.g. {@code CORS_ALLOWED_ORIGINS=https://app.example.com}.
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        Long maxAge
) {

    private static final List<String> DEFAULT_METHODS = List.of("GET");
    private static final long DEFAULT_MAX_AGE_SECONDS = 3600;

    public CorsProperties {
        allowedOrigins = Objects.isNull(allowedOrigins) ? List.of() : List.copyOf(allowedOrigins);
        allowedMethods = Objects.isNull(allowedMethods) || allowedMethods.isEmpty()
                ? DEFAULT_METHODS
                : List.copyOf(allowedMethods);
        maxAge = Objects.isNull(maxAge) ? DEFAULT_MAX_AGE_SECONDS : maxAge;
    }
}
