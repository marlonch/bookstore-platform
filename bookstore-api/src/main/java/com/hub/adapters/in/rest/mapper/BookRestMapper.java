package com.hub.adapters.in.rest.mapper;

import com.hub.adapters.in.rest.dto.request.CreateBookRequest;
import com.hub.adapters.in.rest.dto.request.UpdateBookRequest;
import com.hub.adapters.in.rest.dto.response.BookResponse;
import com.hub.application.catalog.book.port.in.command.CreateBookCommand;
import com.hub.application.catalog.book.port.in.command.UpdateBookCommand;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.ISBN;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookRestMapper {

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId().value(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishedYear(),
                book.getOwnerId().map(o -> o.value()).orElse(null),
                book.getPrice(),
                book.getIsbn().getValue(),
                book.getStatus());
    }

    public CreateBookCommand toCreateCommand(CreateBookRequest request) {
        int initialStock = request.initialStock() != null ? request.initialStock() : 1;
        return new CreateBookCommand(
                request.title(), request.author(), request.publishedYear(),
                request.price(), new ISBN(request.isbn()), initialStock);
    }

    public UpdateBookCommand toUpdateCommand(UUID id, UpdateBookRequest request) {
        return new UpdateBookCommand(
                new BookId(id), request.title(), request.author(), request.publishedYear(), request.price());
    }
}
