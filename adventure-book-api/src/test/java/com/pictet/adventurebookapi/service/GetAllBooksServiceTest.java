package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.config.BooksSingleton;
import com.pictet.adventurebookapi.config.PostConstructionSetup;
import com.pictet.adventurebookapi.model.*;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllBooksServiceTest {

    @Mock
    private JsonParser jsonParser;

    private GetAllBooksService getAllBooksService;

    @BeforeEach
    void beforeEach() {
        resetBooksSingleton();
        getAllBooksService = new GetAllBooksService(jsonParser);
    }

    @AfterEach
    void afterEach() {
        resetBooksSingleton();
    }

    // application startup

    @Test
    void when_application_context_starts__then_books_are_loaded_and_cached() throws IOException {
        // GIVEN
        stubBooks(validBook(1, "Book 1"), validBook(2, "Book 2"), validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        try (AnnotationConfigApplicationContext ignored = startContext()) {

            // THEN
            List<BookSummary> expected = List.of(
                    new BookSummary(1, "Book 1"),
                    new BookSummary(2, "Book 2"),
                    new BookSummary(3, "Book 3"),
                    new BookSummary(4, "Book 4"));

            assertEquals(expected, BooksSingleton.getInstance().getBookSummaries());
            assertEquals(Set.of(1, 2, 3, 4), BooksSingleton.getInstance().getBooks().keySet());
        }
    }

    @Test
    void when_books_are_requested_after_startup__then_cached_summaries_are_returned_without_parsing_again() throws IOException {
        // GIVEN
        stubBooks(validBook(1, "Book 1"), validBook(2, "Book 2"), validBook(3, "Book 3"), validBook(4, "Book 4"));

        try (AnnotationConfigApplicationContext context = startContext()) {
            final GetAllBooksService service = context.getBean(GetAllBooksService.class);

            // WHEN
            final List<BookSummary> summaries = service.getAllBooks();

            // THEN
            assertEquals(4, summaries.size());
            for (AvailableBooks availableBook : AvailableBooks.values()) {
                verify(jsonParser, times(1)).parseJson(availableBook.getFileName(), Book.class);
            }
        }
    }

    @Test
    void when_parser_fails_at_startup__then_application_context_does_not_start() throws IOException {
        // GIVEN
        final IOException ioException = new IOException("boom");
        when(jsonParser.parseJson(AvailableBooks.values()[0].getFileName(), Book.class)).thenThrow(ioException);

        try (AnnotationConfigApplicationContext context = newContext()) {

            // WHEN
            final BeanCreationException exception = assertThrows(BeanCreationException.class, context::refresh);

            // THEN
            assertSame(ioException, exception.getRootCause());
        }
    }

    // business rules validation

    @Test
    void when_all_books_are_invalid__then_empty_list_is_returned() throws IOException {
        // GIVEN
        stubBooks(
                book(1, section(1, SectionTypeEnum.NODE, 2), endSection(2)),
                book(2, beginSection(1, 2), section(2, SectionTypeEnum.NODE, 1)),
                book(3, beginSection(1, 99), endSection(2)),
                book(4, beginSection(1, 3), beginSection(2, 3), endSection(3)));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertTrue(summaries.isEmpty());
        assertTrue(BooksSingleton.getInstance().getBooks().isEmpty());
    }

    @Test
    void when_book_has_no_begin_section__then_it_is_excluded() throws IOException {
        // GIVEN
        final Book noBegin = book(2,
                section(1, SectionTypeEnum.NODE, 2),
                endSection(2));
        stubBooks(validBook(1, "Book 1"), noBegin, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_book_has_more_than_one_begin_section__then_it_is_excluded() throws IOException {
        // GIVEN
        final Book twoBegins = book(2,
                beginSection(1, 3),
                beginSection(2, 3),
                endSection(3));
        stubBooks(validBook(1, "Book 1"), twoBegins, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_book_has_no_end_section__then_it_is_excluded() throws IOException {
        // GIVEN
        final Book noEnd = book(2,
                beginSection(1, 2),
                section(2, SectionTypeEnum.NODE, 1));
        stubBooks(validBook(1, "Book 1"), noEnd, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_book_has_multiple_end_sections__then_it_is_included() throws IOException {
        // GIVEN
        final Book multipleEnds = book(2,
                beginSection(1, 2, 3),
                endSection(2),
                endSection(3));
        stubBooks(validBook(1, "Book 1"), multipleEnds, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(2, "Book 2"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_option_points_to_non_existing_section__then_book_is_excluded() throws IOException {
        // GIVEN
        final Book invalidGoto = book(2,
                beginSection(1, 2),
                section(2, SectionTypeEnum.NODE, 99),
                endSection(3));
        stubBooks(validBook(1, "Book 1"), invalidGoto, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_node_section_has_null_options__then_book_is_excluded() throws IOException {
        // GIVEN
        final Book nodeWithoutOptions = book(2,
                beginSection(1, 2, 3),
                new Section(2, "text", SectionTypeEnum.NODE, null),
                endSection(3));
        stubBooks(validBook(1, "Book 1"), nodeWithoutOptions, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    @Test
    void when_node_section_has_empty_options__then_book_is_excluded() throws IOException {
        // GIVEN
        final Book nodeWithEmptyOptions = book(2,
                beginSection(1, 2, 3),
                new Section(2, "text", SectionTypeEnum.NODE, List.of()),
                endSection(3));
        stubBooks(validBook(1, "Book 1"), nodeWithEmptyOptions, validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(List.of(
                new BookSummary(1, "Book 1"),
                new BookSummary(3, "Book 3"),
                new BookSummary(4, "Book 4")), summaries);
    }

    // cache tests

    @Test
    void when_books_are_loaded__then_only_valid_books_are_stored_by_id() throws IOException {
        // GIVEN
        final Book book1 = validBook(1, "Book 1");
        final Book invalid = book(2, beginSection(1, 99), endSection(2));
        final Book book3 = validBook(3, "Book 3");
        final Book book4 = validBook(4, "Book 4");
        stubBooks(book1, invalid, book3, book4);

        // WHEN
        getAllBooksService.getAllBooks();

        // THEN
        final Map<Integer, Book> cached = BooksSingleton.getInstance().getBooks();
        assertEquals(Set.of(1, 3, 4), cached.keySet());
        assertSame(book1, cached.get(1));
        assertSame(book3, cached.get(3));
        assertSame(book4, cached.get(4));
    }

    @Test
    void when_books_are_loaded__then_summaries_are_stored_in_singleton() throws IOException {
        // GIVEN
        stubBooks(validBook(1, "Book 1"), validBook(2, "Book 2"), validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(summaries, BooksSingleton.getInstance().getBookSummaries());
    }

    @Test
    void when_called_twice__then_book_files_are_parsed_only_once() throws IOException {
        // GIVEN
        stubBooks(validBook(1, "Book 1"), validBook(2, "Book 2"), validBook(3, "Book 3"), validBook(4, "Book 4"));

        // WHEN
        final List<BookSummary> first = getAllBooksService.getAllBooks();
        final List<BookSummary> second = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(first, second);
        for (AvailableBooks availableBook : AvailableBooks.values()) {
            verify(jsonParser, times(1)).parseJson(availableBook.getFileName(), Book.class);
        }
    }

    @Test
    void when_summaries_are_already_cached__then_they_are_returned_without_parsing() {
        // GIVEN
        final List<Book> cached = List.of(new Book(7, "Cached book", null, null));
        BooksSingleton.getInstance().setupBooks(cached);

        // WHEN
        final List<BookSummary> summaries = getAllBooksService.getAllBooks();

        // THEN
        assertEquals(cached.size(), summaries.size());
        verifyNoInteractions(jsonParser);
    }

    @Test
    void when_all_books_are_invalid__then_book_files_are_parsed_again_on_next_call() throws IOException {
        // GIVEN
        final Book invalid = book(1, beginSection(1, 99), endSection(2));
        stubBooks(invalid, invalid, invalid, invalid);

        // WHEN
        getAllBooksService.getAllBooks();
        getAllBooksService.getAllBooks();

        // THEN
        for (AvailableBooks availableBook : AvailableBooks.values()) {
            verify(jsonParser, times(2)).parseJson(availableBook.getFileName(), Book.class);
        }
    }

    // Error scenarios

    @Test
    void when_parser_throws_io_exception__then_runtime_exception_is_thrown() throws IOException {
        // GIVEN
        final IOException ioException = new IOException("boom");
        when(jsonParser.parseJson(AvailableBooks.values()[0].getFileName(), Book.class)).thenThrow(ioException);

        // WHEN
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getAllBooksService.getAllBooks());

        // THEN
        assertSame(ioException, exception.getCause());
    }

    @Test
    void when_parser_throws_io_exception__then_summaries_are_not_cached() throws IOException {
        // GIVEN
        when(jsonParser.parseJson(AvailableBooks.values()[0].getFileName(), Book.class)).thenReturn(validBook(1, "Book 1"));
        when(jsonParser.parseJson(AvailableBooks.values()[1].getFileName(), Book.class)).thenThrow(new IOException("boom"));

        // WHEN
        assertThrows(RuntimeException.class, () -> getAllBooksService.getAllBooks());

        // THEN
        assertTrue(BooksSingleton.getInstance().getBookSummaries().isEmpty());
        verify(jsonParser, never()).parseJson(AvailableBooks.values()[2].getFileName(), Book.class);
    }

    private static void resetBooksSingleton() {
        ReflectionTestUtils.setField(BooksSingleton.class, "INSTANCE", null);
    }

    private AnnotationConfigApplicationContext newContext() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(JsonParser.class, () -> jsonParser);
        context.register(GetAllBooksService.class, PostConstructionSetup.class);
        return context;
    }

    private AnnotationConfigApplicationContext startContext() {
        final AnnotationConfigApplicationContext context = newContext();
        context.refresh();
        return context;
    }

    private void stubBooks(Book... books) throws IOException {
        final AvailableBooks[] availableBooks = AvailableBooks.values();
        assertEquals(availableBooks.length, books.length,
                "One stubbed book is required per AvailableBooks value");
        for (int i = 0; i < availableBooks.length; i++) {
            when(jsonParser.parseJson(availableBooks[i].getFileName(), Book.class)).thenReturn(books[i]);
        }
    }

    private static Book validBook(int id, String title) {
        return new Book(id, title, "Author", List.of(
                beginSection(1, 2),
                section(2, SectionTypeEnum.NODE, 3),
                endSection(3)));
    }

    private static Book book(int id, Section... sections) {
        return new Book(id, "Book " + id, "Author", List.of(sections));
    }

    private static Section beginSection(int id, Integer... gotoIds) {
        return section(id, SectionTypeEnum.BEGIN, gotoIds);
    }

    private static Section endSection(int id) {
        return new Section(id, "The end", SectionTypeEnum.END, null);
    }

    private static Section section(int id, SectionTypeEnum type, Integer... gotoIds) {
        final List<Option> options = Arrays.stream(gotoIds)
                .map(gotoId -> new Option("Go to " + gotoId, gotoId, null))
                .toList();
        return new Section(id, "text", type, options);
    }
}
