package com.example.library;

import com.example.library.entity.Book;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void getAllBooks_returnList() {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Tạo 2 Book giả
        // 2. Mock: when(bookRepository.findAll()).thenReturn(...)
        // 3. Gọi bookService.getAllBooks()
        // 4. Assert kết quả có 2 phần tử
    }

    @Test
    void getBookById_found() {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Tạo 1 Book giả với id = 1L
        // 2. Mock: when(bookRepository.findById(1L)).thenReturn(Optional.of(...))
        // 3. Gọi bookService.getBookById(1L)
        // 4. Assert Book trả về đúng
    }

    @Test
    void getBookById_notFound() {
        // TODO: Sinh viên viết test tại đây
        // Gợi ý:
        // 1. Mock: when(bookRepository.findById(99L)).thenReturn(Optional.empty())
        // 2. Assert: assertThrows(BookNotFoundException.class, ...)
    }
}
