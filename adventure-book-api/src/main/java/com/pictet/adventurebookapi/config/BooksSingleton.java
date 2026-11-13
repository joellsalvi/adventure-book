package com.pictet.adventurebookapi.config;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.model.mapper.BookMapper;

import java.util.*;

public final class BooksSingleton {

    private static BooksSingleton INSTANCE;
    private final List<BookSummary> bookSummaries = new ArrayList<>();
    private final Map<Integer, Book> books = new HashMap<>();

    private BooksSingleton() {}

    public static synchronized BooksSingleton getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = new BooksSingleton();
        }
        return INSTANCE;
    }

    public synchronized void setupBooks(List<Book> bookList) {
        List<Book> books = List.copyOf(bookList);
        if (!books.isEmpty() && INSTANCE.bookSummaries.isEmpty()) {
            INSTANCE.bookSummaries.addAll(BookMapper.mapToSummaryList(books));
            books.forEach(book -> INSTANCE.books.put(book.id(), book));
        }
    }

    public List<BookSummary> getBookSummaries() {
        return List.copyOf(bookSummaries);
    }

    public Map<Integer, Book> getBooks() {
        return Map.copyOf(books);
    }
}
