package com.pictet.adventurebookapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Consequence(
        ConsequenceTypeEnum type,
        String value,
        String text
) {}
