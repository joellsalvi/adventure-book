package com.pictet.adventurebookapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Book(
        Integer id,
        String title,
        String author,
        List<Section> sections
) {}
