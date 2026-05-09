package com.hub.adapters.in.rest.controller;

import com.hub.adapters.in.rest.dto.request.CreateBookRequest;
import com.hub.adapters.in.rest.dto.request.UpdateBookRequest;
import com.hub.adapters.in.rest.dto.response.BookResponse;
import com.hub.adapters.in.rest.mapper.BookRestMapper;
import com.hub.application.catalog.port.in.*;
import com.hub.application.catalog.port.in.command.AssignBookCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final CreateBookUseCase createBookUseCase;
    private final GetBookUseCase getBookUseCase;
    private final UpdateBookUseCase updateBookUseCase;
    private final DeleteBookUseCase deleteBookUseCase;
    private final ListBooksUseCase listBooksUseCase;
    private final AssignBookToUserUseCase assignBookToUserUseCase;
    private final BookRestMapper bookMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookResponse>> listBooks() {
        return ResponseEntity.ok(listBooksUseCase.listBooks().stream()
                .map(bookMapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookMapper.toResponse(getBookUseCase.getBook(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookMapper.toResponse(createBookUseCase.createBook(bookMapper.toCreateCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {
        return ResponseEntity.ok(bookMapper.toResponse(
                updateBookUseCase.updateBook(bookMapper.toUpdateCommand(id, request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        deleteBookUseCase.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookId}/assign/{userId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<BookResponse> assignBookToUser(@PathVariable Long bookId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(bookMapper.toResponse(
                assignBookToUserUseCase.assignBookToUser(new AssignBookCommand(bookId, userId))));
    }
}
