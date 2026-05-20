package com.hub.bookstoreorderservice.application.order.port.out;

import com.hub.bookstoreorderservice.application.order.port.out.dto.BookDetails;
import java.util.UUID;

/**
 * Output port for retrieving book information from the catalog service.
 * Implementations are responsible for fetching and mapping remote book data.
 */
public interface BookValidationPort {

    /**
     * Retrieves book details needed to validate and price an order.
     *
     * @param bookId the catalog identifier of the book
     * @return a {@link BookDetails} snapshot with id, title, and current price
     * @throws com.hub.bookstoreorderservice.domain.exception.InvalidOrderException
     *         if the book does not exist in the catalog
     */
    BookDetails getBook(UUID bookId);
}
