package com.pictet.adventurebookapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Section(
        Integer id,
        String text,
        SectionTypeEnum type,
        List<Option> options
) {}
