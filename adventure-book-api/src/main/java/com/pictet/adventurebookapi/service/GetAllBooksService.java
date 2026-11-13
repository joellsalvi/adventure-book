package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.config.BooksSingleton;
import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.model.validator.BookValidator;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllBooksService {

    private final JsonParser jsonParser;

    public List<BookSummary> getAllBooks() {

        List<BookSummary> bookSummaries = BooksSingleton.getInstance().getBookSummaries();

        if (!CollectionUtils.isEmpty(bookSummaries)) {
            return bookSummaries;
        }

        List<Book> books= new ArrayList<>();

        try {
            for (AvailableBooks availableBook : AvailableBooks.values()) {
                var book = jsonParser.parseJson(availableBook.getFileName(), Book.class);
                if (BookValidator.BOOK_VALIDATOR.isValid(book)) {
                    books.add(book);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BooksSingleton.getInstance().setupBooks(books);
        return BooksSingleton.getInstance().getBookSummaries();
    }

}
