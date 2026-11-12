package com.pictet.adventurebookapi.controller;

import com.pictet.adventurebookapi.model.Book;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.service.GetAllBooksService;
import com.pictet.adventurebookapi.service.GetBookByIdService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
public class BookController {

    private final GetAllBooksService getAllBooksService;
    private final GetBookByIdService getBookByIdService;

    @GetMapping
    public List<BookSummary> getBooks() {
        return getAllBooksService.getAllBooks();
    }

    @GetMapping("/{bookId}")
    public Book getBookById(@PathVariable final Integer bookId) {
        return getBookByIdService.getById(bookId);
    }
}
