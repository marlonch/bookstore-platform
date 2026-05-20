package com.hub.bookstoreorderservice.adapters.out.catalog.feign;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Local DTO representing the subset of {@code BookResponse} fields that
 * order-service needs from bookstore-api. Deliberately minimal to reduce
 * coupling to the remote API contract.
 */
public record BookDto(UUID id, String title, BigDecimal price) {}
