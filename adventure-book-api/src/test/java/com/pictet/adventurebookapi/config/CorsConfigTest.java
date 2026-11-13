package com.pictet.adventurebookapi.config;

import com.pictet.adventurebookapi.controller.BookController;
import com.pictet.adventurebookapi.model.BookSummary;
import com.pictet.adventurebookapi.service.GetAllBooksService;
import com.pictet.adventurebookapi.service.GetBookByIdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class CorsConfigTest {

    private static final String ANGULAR_ORIGIN = "http://localhost:4200";
    private static final String REACT_ORIGIN = "http://localhost:3000";
    private static final String UNKNOWN_ORIGIN = "http://unknown.example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllBooksService getAllBooksService;

    @MockitoBean
    private GetBookByIdService getBookByIdService;

    @ParameterizedTest
    @ValueSource(strings = {ANGULAR_ORIGIN, REACT_ORIGIN})
    void when_preflight_comes_from_a_frontend__then_it_is_allowed(final String origin) throws Exception {
        // WHEN / THEN
        mockMvc.perform(options("/books")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600"));
    }

    @Test
    void when_get_comes_from_a_frontend__then_response_allows_its_origin() throws Exception {
        // GIVEN
        when(getAllBooksService.getAllBooks()).thenReturn(List.of(new BookSummary(1, "The Prisoner")));

        // WHEN / THEN
        mockMvc.perform(get("/books").header(HttpHeaders.ORIGIN, ANGULAR_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ANGULAR_ORIGIN));
    }

    @Test
    void when_preflight_comes_from_an_unknown_origin__then_it_is_rejected() throws Exception {
        // WHEN / THEN
        mockMvc.perform(options("/books")
                        .header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void when_get_comes_from_an_unknown_origin__then_it_is_rejected() throws Exception {
        // WHEN / THEN
        mockMvc.perform(get("/books").header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void when_preflight_asks_for_a_method_not_allowed__then_it_is_rejected() throws Exception {
        // WHEN / THEN
        mockMvc.perform(options("/books/1")
                        .header(HttpHeaders.ORIGIN, ANGULAR_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void when_request_has_no_origin__then_it_is_served_without_cors_headers() throws Exception {
        // GIVEN
        when(getAllBooksService.getAllBooks()).thenReturn(List.of());

        // WHEN / THEN
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
