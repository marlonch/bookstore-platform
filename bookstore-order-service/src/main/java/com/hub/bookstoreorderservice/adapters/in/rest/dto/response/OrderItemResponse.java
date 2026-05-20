package com.hub.bookstoreorderservice.adapters.in.rest.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(UUID bookId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
