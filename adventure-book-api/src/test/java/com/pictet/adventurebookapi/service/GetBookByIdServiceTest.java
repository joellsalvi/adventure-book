package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.exception.BookNotFoundException;
import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookByIdServiceTest {

    private static final AvailableBooks FIRST = AvailableBooks.values()[0];
    private static final AvailableBooks SECOND = AvailableBooks.values()[1];
    private static final AvailableBooks LAST = AvailableBooks.values()[AvailableBooks.values().length - 1];

    @Mock
    private JsonParser jsonParser;

    private GetBookByIdService getBookByIdService;

    @BeforeEach
    void setUp() {
        getBookByIdService = new GetBookByIdService(jsonParser);
    }

    @Test
    void when_id_matches_first_book__then_it_is_returned_without_parsing_others() throws IOException {
        // GIVEN
        final Book expected = book(1);
        when(jsonParser.parseJson(FIRST, Book.class)).thenReturn(expected);

        // WHEN
        final Book result = getBookByIdService.getById(1);

        // THEN
        assertSame(expected, result);
        verify(jsonParser, never()).parseJson(SECOND, Book.class);
        verify(jsonParser, never()).parseJson(LAST, Book.class);
    }

    @Test
    void when_id_matches_last_book__then_it_is_returned() throws IOException {
        // GIVEN
        final List<Book> books = stubAllBooks(1, 2, 3, 4);

        // WHEN
        final Book result = getBookByIdService.getById(4);

        // THEN
        assertSame(books.get(books.size() - 1), result);
    }

    @Test
    void when_id_does_not_match_any_book__then_book_not_found_exception_is_thrown() throws IOException {
        // GIVEN
        stubAllBooks(1, 2, 3, 4);

        // WHEN
        final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> getBookByIdService.getById(99));

        // THEN
        assertEquals("Book not found! ID: 99", exception.getMessage());
    }

    @Test
    void when_id_is_null__then_book_not_found_exception_is_thrown() throws IOException {
        // GIVEN
        stubAllBooks(1, 2, 3, 4);

        // WHEN
        final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> getBookByIdService.getById(null));

        // THEN
        assertEquals("Book not found! ID: null", exception.getMessage());
    }

    @Test
    void when_parser_throws_io_exception__then_runtime_exception_is_thrown() throws IOException {
        // GIVEN
        final IOException ioException = new IOException("boom");
        when(jsonParser.parseJson(FIRST, Book.class)).thenThrow(ioException);

        // WHEN
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getBookByIdService.getById(1));

        // THEN
        assertSame(ioException, exception.getCause());
    }

    @Test
    void when_parser_throws_io_exception_after_non_matching_book__then_runtime_exception_is_thrown() throws IOException {
        // GIVEN
        final IOException ioException = new IOException("boom");
        when(jsonParser.parseJson(FIRST, Book.class)).thenReturn(book(1));
        when(jsonParser.parseJson(SECOND, Book.class)).thenThrow(ioException);

        // WHEN
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getBookByIdService.getById(3));

        // THEN
        assertSame(ioException, exception.getCause());
        verify(jsonParser, never()).parseJson(LAST, Book.class);
    }

    @Test
    void when_id_matches_middle_book__then_it_is_returned_and_remaining_books_are_not_parsed() throws IOException {
        // GIVEN
        final List<Book> books = stubAllBooks(1, 2, 3, 4);

        // WHEN
        final Book result = getBookByIdService.getById(2);

        // THEN
        assertSame(books.get(1), result);
        verify(jsonParser, never()).parseJson(LAST, Book.class);
    }

    // ---------- helpers ----------

    // Lenient: tests that return early do not consume every stub.
    private List<Book> stubAllBooks(Integer... ids) throws IOException {
        final AvailableBooks[] availableBooks = AvailableBooks.values();
        assertEquals(availableBooks.length, ids.length,
                "One stubbed id is required per AvailableBooks value");
        final List<Book> books = new ArrayList<>();
        for (int i = 0; i < availableBooks.length; i++) {
            final Book book = book(ids[i]);
            books.add(book);
            lenient().when(jsonParser.parseJson(availableBooks[i], Book.class)).thenReturn(book);
        }
        return books;
    }

    private static Book book(Integer id) {
        return new Book(id, "Book " + id, "Author", List.of());
    }
}
