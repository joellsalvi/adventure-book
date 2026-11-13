package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.parser.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the validity rules on the real book files instead of stubs. Also fails
 * when a book uses a consequence type missing from ConsequenceTypeEnum, since
 * one book that cannot be parsed breaks the whole list.
 */
class GetAllBooksServiceBookFilesTest {

    @Test
    void when_reading_the_book_files__then_only_valid_books_are_listed() {
        // GIVEN
        final GetAllBooksService getAllBooksService = new GetAllBooksService(new JsonParser());

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "The Prisoner"),
                new BookSummary(4, "The Prisoner: The Long Night")), summaries);
    }

}
