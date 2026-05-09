package com.hub.app.service;

import com.hub.application.catalog.port.in.command.AssignBookCommand;
import com.hub.application.catalog.port.in.command.CreateBookCommand;
import com.hub.application.catalog.port.out.BookRepositoryPort;
import com.hub.application.catalog.service.BookService;

import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.catalog.Book;
import com.hub.domain.catalog.exception.BookNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepositoryPort bookRepository;
    @Mock
    UserRepositoryPort userRepository;

    @InjectMocks
    BookService bookService;

    @Test
    void createBook_savesAndReturnsBook() {
        CreateBookCommand cmd = new CreateBookCommand("Clean Code", "Robert Martin", 2008);
        Book saved = Book.createNew( "Clean Code","Robert Martin",2008);
        when(bookRepository.save(any())).thenReturn(saved);

        Book result = bookService.createBook(cmd);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void getBook_withExistingId_returnsBook() {
        when(bookRepository.findById(1L)).thenReturn(
                Optional.of(Book.existing(1L,"DDD","Marlon Cardenas",2004,2L))
        );
        assertThat(bookService.getBook(1L).getTitle()).isEqualTo("DDD");
    }

    @Test
    void getBook_withMissingId_throwsBookNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.getBook(99L)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void deleteBook_withMissingId_throwsBookNotFoundException() {
        when(bookRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> bookService.deleteBook(99L)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void assignBookToUser_withValidIds_updatesOwner() {
        Book book = Book.existing(1L,"DDD","Marlon Cardenas",2004,2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.existsById(5L)).thenReturn(true);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.assignBookToUser(new AssignBookCommand(1L, 5L));

        assertThat(result.getOwnerId()).contains(5L);
    }

    @Test
    void assignBookToUser_withMissingUser_throwsUserNotFoundException() {
        Book book = Book.existing(1L,"DDD","Marlon Cardenas",2004,2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.assignBookToUser(new AssignBookCommand(1L, 99L)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUserBooks_withMissingUser_throwsUserNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> bookService.listUserBooks(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUserBooks_returnsOnlyUserBooks() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookRepository.findByOwnerId(2L)).thenReturn(List.of(
               Book.existing(1L,"DDD","Marlon Cardenas",2004,2L)
        ));
        assertThat(bookService.listUserBooks(2L)).hasSize(1);
    }
}
