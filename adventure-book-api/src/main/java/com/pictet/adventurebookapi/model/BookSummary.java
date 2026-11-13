package com.pictet.adventurebookapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookSummary(
        Integer id,
        String title
) {}
