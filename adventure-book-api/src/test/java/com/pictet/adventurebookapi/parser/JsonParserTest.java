package com.pictet.adventurebookapi.parser;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.util.AvailableBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonParserTest {

    private JsonParser jsonParser;

    @BeforeEach
    void setUp() {
        jsonParser = new JsonParser();
    }

    @Test
    void when_deserialize_the_prisoner__then_title_is_mapped() throws IOException {
        // GIVEN
        final var availableBook = AvailableBooks.BOOk_THE_PRISONER;

        // WHEN
        final Book book = jsonParser.parseJson(availableBook, Book.class);

        // THEN
        assertNotNull(book);
        assertEquals(1, book.id());
        assertEquals("The Prisoner", book.title());
        assertEquals("Daniel El Fuego", book.author());
        assertNotNull(book.sections());
    }

    @Test
    void when_deserialize_the_prisoner_all_errors__then_title_is_mapped() throws IOException {
        // GIVEN
        final var availableBook = AvailableBooks.BOOk_THE_PRISONER_ALL_ERRORS;

        // WHEN
        final Book book = jsonParser.parseJson(availableBook, Book.class);

        // THEN
        assertNotNull(book);
        assertEquals(2, book.id());
        assertEquals("The Prisoner (draft 1)", book.title());
        assertEquals("Daniel El Fuego", book.author());
        assertNotNull(book.sections());
    }

    @Test
    void when_deserialize_the_prisoner_bad_id__then_title_is_mapped() throws IOException {
        // GIVEN
        final var availableBook = AvailableBooks.BOOk_THE_PRISONER_BAD_ID;

        // WHEN
        final Book book = jsonParser.parseJson(availableBook, Book.class);

        // THEN
        assertNotNull(book);
        assertEquals(3, book.id());
        assertEquals("The Prisoner (draft 2)", book.title());
        assertEquals("Daniel El Fuego", book.author());
        assertNotNull(book.sections());
    }

    @Test
    void when_deserialize_book_summary__then_title_is_mapped() throws IOException {
        // GIVEN
        final var availableBook = AvailableBooks.BOOk_THE_PRISONER;

        // WHEN
        final BookSummary bookSummary = jsonParser.parseJson(availableBook, BookSummary.class);

        // THEN
        assertNotNull(bookSummary);
        assertEquals(1, bookSummary.id());
        assertEquals("The Prisoner", bookSummary.title());
    }

}
