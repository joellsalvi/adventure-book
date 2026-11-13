package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.config.BooksSingleton;
import com.pictet.adventurebookapi.config.PostConstructionSetup;
import com.pictet.adventurebookapi.exception.BookNotFoundException;
import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.parser.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetBookByIdServiceTest {

    private GetBookByIdService getBookByIdService;

    @BeforeEach
    void beforeEach() {
        resetBooksSingleton();
        getBookByIdService = new GetBookByIdService();
    }

    @AfterEach
    void afterEach() {
        resetBooksSingleton();
    }

    @Test
    void when_id_matches_cached_book__then_it_is_returned() {
        // GIVEN
        final Book expected = book(1);
        cacheBooks(expected);

        // WHEN
        final Book result = getBookByIdService.getById(1);

        // THEN
        assertSame(expected, result);
    }

    @Test
    void when_several_books_are_cached__then_the_matching_one_is_returned() {
        // GIVEN
        final Book book1 = book(1);
        final Book book2 = book(2);
        final Book book4 = book(4);
        cacheBooks(book1, book2, book4);

        // WHEN
        final Book result = getBookByIdService.getById(2);

        // THEN
        assertSame(book2, result);
    }

    @Test
    void when_id_does_not_match_any_cached_book__then_book_not_found_exception_is_thrown() {
        // GIVEN
        cacheBooks(book(1), book(2), book(4));

        // WHEN
        final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> getBookByIdService.getById(99));

        // THEN
        assertEquals("Book not found! ID: 99", exception.getMessage());
    }

    @Test
    void when_cache_is_empty__then_book_not_found_exception_is_thrown() {
        // GIVEN
        // nothing cached

        // WHEN
        final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> getBookByIdService.getById(1));

        // THEN
        assertEquals("Book not found! ID: 1", exception.getMessage());
    }

    @Test
    void when_id_is_null__then_book_not_found_exception_is_thrown() {
        // GIVEN
        cacheBooks(book(1), book(2));

        // WHEN
        final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> getBookByIdService.getById(null));

        // THEN
        assertEquals("Book not found! ID: null", exception.getMessage());
    }

    // tests with real book files

    @Test
    void when_application_context_starts__then_valid_book_is_found_without_listing_books_first() {
        // GIVEN
        try (AnnotationConfigApplicationContext context = startContext()) {
            final GetBookByIdService service = context.getBean(GetBookByIdService.class);

            // WHEN
            final Book result = service.getById(4);

            // THEN
            assertEquals(4, result.id());
            assertEquals("The Prisoner: The Long Night", result.title());
        }
    }

    @Test
    void when_application_context_starts__then_invalid_book_is_not_found() {
        // GIVEN
        try (AnnotationConfigApplicationContext context = startContext()) {
            final GetBookByIdService service = context.getBean(GetBookByIdService.class);

            // WHEN
            final BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                    () -> service.getById(2));

            // THEN
            assertEquals("Book not found! ID: 2", exception.getMessage());
        }
    }

    private static void resetBooksSingleton() {
        ReflectionTestUtils.setField(BooksSingleton.class, "INSTANCE", null);
    }

    private static AnnotationConfigApplicationContext startContext() {
        return new AnnotationConfigApplicationContext(
                JsonParser.class, GetAllBooksService.class, GetBookByIdService.class, PostConstructionSetup.class);
    }

    private static void cacheBooks(Book... books) {
        BooksSingleton.getInstance().setupBooks(List.of(books));
    }

    private static Book book(Integer id) {
        return new Book(id, "Book " + id, "Author", List.of());
    }
}
