package com.pictet.adventurebookapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Option(
        String description,
        Integer gotoId,
        Consequence consequence
) {}
