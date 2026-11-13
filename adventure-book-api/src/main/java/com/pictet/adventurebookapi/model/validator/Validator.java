package com.pictet.adventurebookapi.model.validator;

@FunctionalInterface
public interface Validator<T> {

    boolean isValid(T t);

}
