package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.config.BooksSingleton;
import com.pictet.adventurebookapi.exception.BookNotFoundException;
import com.pictet.adventurebookapi.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBookByIdService {

    public Book getById(Integer id) {

        if (Objects.isNull(id)) {
            throw new BookNotFoundException("Book not found! ID: " + id);
        }

        Book bookFound = BooksSingleton.getInstance()
                .getBooks()
                .get(id);

        if (Objects.isNull(bookFound)) {
            throw new BookNotFoundException("Book not found! ID: " + id);
        }

        return bookFound;
    }

}
