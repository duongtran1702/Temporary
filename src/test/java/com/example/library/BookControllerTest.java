package com.example.library;

import com.example.library.controller.BookController;
import com.example.library.entity.Book;
import com.example.library.exception.BookNotFoundException;
import com.example.library.exception.GlobalExceptionHandler;
import com.example.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BookController.class, GlobalExceptionHandler.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturn200AndList() throws Exception {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Mock bookService.getAllBooks() trả về 1 list có sách
        // 2. mockMvc.perform(get("/api/books"))
        // 3. .andExpect(status().isOk())
        // 4. .andExpect(jsonPath("$.length()").value(1)) (hoặc số sách tương ứng)
    }

    @Test
    void getById_found_shouldReturn200() throws Exception {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Mock bookService.getBookById(1L) trả về 1 Book
        // 2. mockMvc.perform(get("/api/books/1"))
        // 3. .andExpect(status().isOk())
        // 4. .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    void getById_notFound_shouldReturn404() throws Exception {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Mock bookService.getBookById(99L) ném BookNotFoundException
        // 2. mockMvc.perform(get("/api/books/99"))
        // 3. .andExpect(status().isNotFound())
    }
}
