package com.hub.application.catalog.service;

import com.hub.application.catalog.port.in.*;
import com.hub.application.catalog.port.in.command.AssignBookCommand;
import com.hub.application.catalog.port.in.command.CreateBookCommand;
import com.hub.application.catalog.port.in.command.UpdateBookCommand;
import com.hub.application.catalog.port.out.BookRepositoryPort;
import com.hub.application.identity.port.out.UserRepositoryPort;
import com.hub.domain.auth.exception.UserNotFoundException;
import com.hub.domain.catalog.Book;
import com.hub.domain.catalog.exception.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService implements CreateBookUseCase, GetBookUseCase, UpdateBookUseCase,
        DeleteBookUseCase, ListBooksUseCase, AssignBookToUserUseCase, ListUserBooksUseCase {

    private final BookRepositoryPort bookRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public Book createBook(CreateBookCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        return bookRepository.save(Book.createNew(
                command.title(),
                command.author(),
                command.publishedYear()));
    }

    @Override
    public Book getBook(Long bookId) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
    }

    @Override
    public Book updateBook(UpdateBookCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Book existing = bookRepository.findById(command.id())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + command.id()));

        Book updated = Book.existing(command.id(), command.title() != null ? command.title() : existing.getTitle(),
                command.author() != null ? command.author() : existing.getAuthor(),
                command.publishedYear() != null ? command.publishedYear() : existing.getPublishedYear(), existing.getOwnerId());
        return bookRepository.save(updated);
    }

    @Override
    public void deleteBook(Long bookId) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException("Book not found: " + bookId);
        }
        bookRepository.deleteById(bookId);
    }

    @Override
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book assignBookToUser(AssignBookCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Book book = bookRepository.findById(command.bookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + command.bookId()));

        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFoundException("User not found: " + command.userId());
        }

        return bookRepository.save(Book.existing(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishedYear(),
                command.userId())
                );
    }

    @Override
    public List<Book> listUserBooks(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        return bookRepository.findByOwnerId(userId);
    }
}
