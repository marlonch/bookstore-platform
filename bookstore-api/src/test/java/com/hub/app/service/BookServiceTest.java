package com.hub.app.service;

import com.hub.application.catalog.port.in.command.AssignBookCommand;
import com.hub.application.catalog.port.in.command.CreateBookCommand;
import com.hub.application.catalog.port.out.BookRepositoryPort;
import com.hub.application.catalog.service.BookService;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.BookStatus;
import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.catalog.exception.BookNotFoundException;
import com.hub.domain.identity.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final BigDecimal PRICE = new BigDecimal("29.99");
    private static final ISBN ISBN = new ISBN("9780134190440");
    private static final BookId BOOK_ID = new BookId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final BookId MISSING_ID = new BookId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
    private static final UserId USER_ID = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final UserId MISSING_USER = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

    @Mock BookRepositoryPort bookRepository;
    @Mock UserRepositoryPort userRepository;

    @InjectMocks
    BookService bookService;

    @Test
    void createBook_savesAndReturnsBook() {
        CreateBookCommand cmd = new CreateBookCommand("Clean Code", "Robert Martin", 2008, PRICE, ISBN);
        Book saved = Book.createNew("Clean Code", "Robert Martin", 2008, PRICE, ISBN);
        when(bookRepository.save(any())).thenReturn(saved);

        Book result = bookService.createBook(cmd);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void getBook_withExistingId_returnsBook() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(
                Optional.of(Book.existing(BOOK_ID, "DDD", "Eric Evans", 2004, PRICE, ISBN, BookStatus.ACTIVE, null)));
        assertThat(bookService.getBook(BOOK_ID).getTitle()).isEqualTo("DDD");
    }

    @Test
    void getBook_withMissingId_throwsBookNotFoundException() {
        when(bookRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.getBook(MISSING_ID)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void deleteBook_withMissingId_throwsBookNotFoundException() {
        when(bookRepository.existsById(MISSING_ID)).thenReturn(false);
        assertThatThrownBy(() -> bookService.deleteBook(MISSING_ID)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void assignBookToUser_withValidIds_updatesOwner() {
        Book book = Book.existing(BOOK_ID, "DDD", "Eric Evans", 2004, PRICE, ISBN, BookStatus.ACTIVE, null);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.assignBookToUser(new AssignBookCommand(BOOK_ID, USER_ID));

        assertThat(result.getOwnerId()).contains(USER_ID);
    }

    @Test
    void assignBookToUser_withMissingUser_throwsUserNotFoundException() {
        Book book = Book.existing(BOOK_ID, "DDD", "Eric Evans", 2004, PRICE, ISBN, BookStatus.ACTIVE, null);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(userRepository.existsById(MISSING_USER)).thenReturn(false);

        assertThatThrownBy(() -> bookService.assignBookToUser(new AssignBookCommand(BOOK_ID, MISSING_USER)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUserBooks_withMissingUser_throwsUserNotFoundException() {
        when(userRepository.existsById(MISSING_USER)).thenReturn(false);
        assertThatThrownBy(() -> bookService.listUserBooks(MISSING_USER)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUserBooks_returnsOnlyUserBooks() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(bookRepository.findByOwnerId(USER_ID)).thenReturn(List.of(
                Book.existing(BOOK_ID, "DDD", "Eric Evans", 2004, PRICE, ISBN, BookStatus.ACTIVE, USER_ID)));
        assertThat(bookService.listUserBooks(USER_ID)).hasSize(1);
    }
}
