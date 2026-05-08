package com.hub.adapters.in.rest.mapper;

import com.hub.adapters.in.rest.dto.request.CreateBookRequest;
import com.hub.adapters.in.rest.dto.request.UpdateBookRequest;
import com.hub.adapters.in.rest.dto.response.BookResponse;
import com.hub.application.catalog.port.in.command.CreateBookCommand;
import com.hub.application.catalog.port.in.command.UpdateBookCommand;
import com.hub.domain.catalog.Book;
import org.springframework.stereotype.Component;

@Component
public class BookRestMapper {

    public BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getPublishedYear(), book.getOwnerId().orElse(null));
    }

    public CreateBookCommand toCreateCommand(CreateBookRequest request) {
        return new CreateBookCommand(request.title(), request.author(), request.publishedYear());
    }

    public UpdateBookCommand toUpdateCommand(Long id, UpdateBookRequest request) {
        return new UpdateBookCommand(id, request.title(), request.author(), request.publishedYear());
    }
}
