package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.model.Option;
import com.pictet.adventurebookapi.model.Section;
import com.pictet.adventurebookapi.model.SectionTypeEnum;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllBooksServiceTest {

    @Mock
    private JsonParser jsonParser;

    private GetAllBooksService getAllBooksService;

    @BeforeEach
    void setUp() {
        getAllBooksService = new GetAllBooksService(jsonParser);
    }

    @Test
    void when_all_books_are_valid__then_all_summaries_are_returned_in_order() throws IOException {
        // GIVEN
        stubBooks(validBook(1, "Book 1"), validBook(2, "Book 2"), validBook(3, "Book 3"), validBook(4, "Book 4"));

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

    @Test
    void when_parser_throws_io_exception__then_runtime_exception_is_thrown() throws IOException {
        // GIVEN
        final IOException ioException = new IOException("boom");
        when(jsonParser.parseJson(AvailableBooks.values()[0], Book.class)).thenThrow(ioException);

        // WHEN
        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> getAllBooksService.getAllBooks());

        // THEN
        assertInstanceOf(IOException.class, exception.getCause());
        assertSame(ioException, exception.getCause());
    }

    // ---------- helpers ----------

    private void stubBooks(Book... books) throws IOException {
        final AvailableBooks[] availableBooks = AvailableBooks.values();
        assertEquals(availableBooks.length, books.length,
                "One stubbed book is required per AvailableBooks value");
        for (int i = 0; i < availableBooks.length; i++) {
            when(jsonParser.parseJson(availableBooks[i], Book.class)).thenReturn(books[i]);
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
