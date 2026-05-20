package com.hub.bookstoreorderservice.adapters.out.catalog.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign declarative client for bookstore-api's book catalog endpoints.
 * Separated from the {@link BookValidationAdapter} to keep the Feign declaration
 * clean and independently testable.
 */
@FeignClient(name = "bookstore-api", url = "${bookstore.api.url}")
public interface BookFeignInterface {

    @GetMapping("/api/books/{id}")
    BookDto getBook(@PathVariable UUID id);
}
