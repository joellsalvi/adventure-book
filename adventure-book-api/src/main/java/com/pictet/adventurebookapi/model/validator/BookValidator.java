package com.pictet.adventurebookapi.model.validator;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.Option;
import com.pictet.adventurebookapi.model.Section;
import com.pictet.adventurebookapi.model.SectionTypeEnum;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.stream.Collectors;

public final class BookValidator implements Validator<Book> {

    public static final BookValidator BOOK_VALIDATOR = new BookValidator();

    @Override
    public boolean isValid(Book book) {
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

}
