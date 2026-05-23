package com.hub.app.service;

import com.hub.application.catalog.book.port.in.command.AssignBookCommand;
import com.hub.application.catalog.book.port.in.command.CreateBookCommand;
import com.hub.application.catalog.book.port.in.command.UpdateBookCommand;
import com.hub.application.catalog.book.port.out.BookRepositoryPort;
import com.hub.application.catalog.service.BookService;
import com.hub.application.catalog.stock.port.out.StockRepositoryPort;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.application.shared.port.out.TransactionPort;
import com.hub.domain.catalog.stock.Stock;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.BookStatus;
import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.catalog.exception.BookNotFoundException;
import com.hub.domain.catalog.exception.DuplicateIsbnException;
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

import org.springframework.dao.DataIntegrityViolationException;

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
    @Mock StockRepositoryPort stockRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock TransactionPort transaction;

    @InjectMocks
    BookService bookService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(transaction.execute(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> ((java.util.function.Supplier<?>) inv.getArgument(0)).get());
        org.mockito.Mockito.lenient().when(stockRepository.save(org.mockito.ArgumentMatchers.any(Stock.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createBook_savesAndReturnsBook() {
        CreateBookCommand cmd = new CreateBookCommand("Clean Code", "Robert Martin", 2008, PRICE, ISBN, 5);
        Book saved = Book.createNew("Clean Code", "Robert Martin", 2008, PRICE, ISBN);
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(false);
        when(bookRepository.save(any())).thenReturn(saved);

        Book result = bookService.createBook(cmd);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void createBook_withDuplicateIsbn_throwsDuplicateIsbnException() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(true);
        CreateBookCommand cmd = new CreateBookCommand("Clean Code", "Robert Martin", 2008, PRICE, ISBN, 5);

        assertThatThrownBy(() -> bookService.createBook(cmd))
                .isInstanceOf(DuplicateIsbnException.class);
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
    void updateBook_withNullCommand_throwsNullPointerException() {
        assertThatThrownBy(() -> bookService.updateBook(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateBook_withMissingId_throwsBookNotFoundException() {
        when(bookRepository.findById(MISSING_ID)).thenReturn(Optional.empty());
        UpdateBookCommand cmd = new UpdateBookCommand(MISSING_ID, "New Title", null, null, null);

        assertThatThrownBy(() -> bookService.updateBook(cmd))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void updateBook_withAllFieldsProvided_updatesAllFields() {
        Book existing = Book.existing(BOOK_ID, "Old Title", "Old Author", 2000, new BigDecimal("10.00"), ISBN, BookStatus.ACTIVE, null);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookCommand cmd = new UpdateBookCommand(BOOK_ID, "New Title", "New Author", 2020, new BigDecimal("49.99"));
        Book result = bookService.updateBook(cmd);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.getPublishedYear()).isEqualTo(2020);
        assertThat(result.getPrice()).isEqualByComparingTo("49.99");
    }

    @Test
    void updateBook_withNullFields_keepsExistingValues() {
        Book existing = Book.existing(BOOK_ID, "Original Title", "Original Author", 2000, new BigDecimal("10.00"), ISBN, BookStatus.ACTIVE, null);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookCommand cmd = new UpdateBookCommand(BOOK_ID, null, null, null, null);
        Book result = bookService.updateBook(cmd);

        assertThat(result.getTitle()).isEqualTo("Original Title");
        assertThat(result.getAuthor()).isEqualTo("Original Author");
        assertThat(result.getPublishedYear()).isEqualTo(2000);
        assertThat(result.getPrice()).isEqualByComparingTo("10.00");
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

    @Test
    void createBook_whenDbViolatesIsbnUniqueness_throwsDuplicateIsbnException() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(false);
        when(bookRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        CreateBookCommand cmd = new CreateBookCommand("Clean Code", "Robert Martin", 2008, PRICE, ISBN, 5);

        assertThatThrownBy(() -> bookService.createBook(cmd))
                .isInstanceOf(DuplicateIsbnException.class);
    }
}
