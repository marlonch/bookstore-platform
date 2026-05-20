package com.hub.bookstoreorderservice.adapters.out.catalog.feign;

import com.hub.bookstoreorderservice.application.order.port.out.BookValidationPort;
import com.hub.bookstoreorderservice.application.order.port.out.dto.BookDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Outbound adapter that implements {@link BookValidationPort} by delegating to
 * {@link BookFeignInterface}. Translates the remote {@link BookDto} into the
 * application-layer {@link BookDetails} value object, keeping the application
 * service decoupled from the HTTP transport.
 */
@Component
@RequiredArgsConstructor
public class BookValidationAdapter implements BookValidationPort {

    private final BookFeignInterface bookFeignInterface;

    @Override
    public BookDetails getBook(UUID bookId) {
        BookDto dto = bookFeignInterface.getBook(bookId);
        return new BookDetails(dto.id(), dto.title(), dto.price());
    }
}
