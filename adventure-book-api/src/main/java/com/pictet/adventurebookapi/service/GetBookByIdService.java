package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.exception.BookNotFoundException;
import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class GetBookByIdService {

    private final JsonParser jsonParser;

    public Book getById(Integer id) {

        try {
            for (AvailableBooks availableBook : AvailableBooks.values()) {
                var book = jsonParser.parseJson(availableBook, Book.class);
                if (book.id().equals(id)) {
                    return book;
                }
            }
            throw new BookNotFoundException("Book not found! ID: " + id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
