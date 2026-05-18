package com.hub.adapters.in.rest.dto.response;

import com.hub.domain.catalog.book.BookStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record BookResponse(UUID id, String title, String author, Integer publishedYear,
                           UUID ownerId, BigDecimal price, String isbn, BookStatus status) {}
