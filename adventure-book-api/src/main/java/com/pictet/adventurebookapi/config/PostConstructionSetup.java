package com.pictet.adventurebookapi.config;

import com.pictet.adventurebookapi.service.GetAllBooksService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostConstructionSetup {

    public final GetAllBooksService getAllBooksService;

    @PostConstruct
    public void initialize() {
        getAllBooksService.getAllBooks();
    }

}
