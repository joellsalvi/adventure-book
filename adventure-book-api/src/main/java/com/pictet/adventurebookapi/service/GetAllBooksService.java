package com.pictet.adventurebookapi.service;

import com.pictet.adventurebookapi.model.*;
import com.pictet.adventurebookapi.parser.JsonParser;
import com.pictet.adventurebookapi.util.AvailableBooks;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class GetAllBooksService {

    private final JsonParser jsonParser;

    public List<BookSummary> getAllBooks() {

        List<Book> books= new ArrayList<>();

        try {
            for (AvailableBooks availableBook : AvailableBooks.values()) {
                var book = jsonParser.parseJson(availableBook, Book.class);
                if (isAValidBook(book)) {
                    books.add(book);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapToSummary(books);
    }

    private boolean isAValidBook(Book book) {

        return !checkBookSectionsWithMoreThanOneBegin(book) &&
                !checkBookSectionsWithoutEnd(book) &&
                !checkOptionsWithInvalidNextSectionId(book) &&
                !checkBookSectionNodeWithoutOptions(book);
    }

    // Book has none, or more than one beginning
    private static boolean checkBookSectionsWithMoreThanOneBegin(Book book) {
        return book.sections().stream()
                .filter(section -> SectionTypeEnum.BEGIN.equals(section.type()))
                .count() != 1;
    }

    // Book has no ending (but can have multiple)
    private static boolean checkBookSectionsWithoutEnd(Book book) {
        return book.sections().stream()
                .noneMatch(section -> SectionTypeEnum.END.equals(section.type()));
    }

    // Book has invalid next section id.
    private static boolean checkOptionsWithInvalidNextSectionId(Book book) {
        boolean hasInvalidNextSectionId = false;
        var validSectionIds = book.sections().stream()
                .map(Section::id)
                .collect(Collectors.toSet());

        for (Section section : book.sections()) {

            if (Objects.isNull(section.options())) {
                continue;
            }

            boolean validSectionOptions = validSectionIds
                    .containsAll(section.options().stream().map(Option::gotoId).toList());

            if (!validSectionOptions) {
                hasInvalidNextSectionId = true;
                break;
            }
        }
        return hasInvalidNextSectionId;
    }

    // A non-ending section has no options
    private static boolean checkBookSectionNodeWithoutOptions(Book book) {
        return book.sections().stream()
                .anyMatch(section ->
                        SectionTypeEnum.NODE.equals(section.type()) && CollectionUtils.isEmpty(section.options()));
    }

    private List<BookSummary> mapToSummary(List<Book> books) {
        return books.stream()
                .map(book -> new BookSummary(book.id(), book.title()))
                .toList();
    }

}
