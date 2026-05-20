package com.hub.bookstoreorderservice.application.order.port.out.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot of book data captured at order creation time.
 * Produced by {@link com.hub.bookstoreorderservice.application.order.port.out.BookValidationPort}
 * and consumed by the order application service.
 */
public record BookDetails(UUID id, String title, BigDecimal price) {}
