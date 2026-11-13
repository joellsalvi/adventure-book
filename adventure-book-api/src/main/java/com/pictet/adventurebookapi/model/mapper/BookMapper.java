package com.pictet.adventurebookapi.model.mapper;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;

import java.util.List;

public interface BookMapper {

    static BookSummary mapToSummary(Book book) {
        return new BookSummary(book.id(), book.title());
    }

    static List<BookSummary> mapToSummaryList(List<Book> books) {
        return books.stream()
                .map(BookMapper::mapToSummary)
                .toList();
    }

}
