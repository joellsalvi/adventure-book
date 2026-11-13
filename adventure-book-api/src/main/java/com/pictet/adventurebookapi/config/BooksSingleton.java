package com.pictet.adventurebookapi.config;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.model.mapper.BookMapper;
import lombok.Getter;

import java.util.*;

@Getter
public final class BooksSingleton {

    private static BooksSingleton INSTANCE;
    private final List<BookSummary> bookSummaries = new ArrayList<>();
    private final Map<Integer, Book> books = new HashMap<>();

    private BooksSingleton() {}

    public static BooksSingleton getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = new BooksSingleton();
        }
        return INSTANCE;
    }

    public void setupBooks(List<Book> books) {
        if (!books.isEmpty() && INSTANCE.bookSummaries.isEmpty()) {
            INSTANCE.bookSummaries.addAll(BookMapper.mapToSummaryList(books));
            books.forEach(book -> INSTANCE.books.put(book.id(), book));
        }
    }

}
