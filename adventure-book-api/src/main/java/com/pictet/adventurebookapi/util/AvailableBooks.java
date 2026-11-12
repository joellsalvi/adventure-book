package com.pictet.adventurebookapi.util;

import lombok.Getter;

@Getter
public enum AvailableBooks {

    BOOk_THE_PRISONER("the-prisoner.json"),
    BOOk_THE_PRISONER_ALL_ERRORS("the-prisoner_all-errors.json"),
    BOOk_THE_PRISONER_BAD_ID("the-prisoner_bad-id.json");

    private final String fileName;

    AvailableBooks(String fileName) {
        this.fileName = fileName;
    }

}
