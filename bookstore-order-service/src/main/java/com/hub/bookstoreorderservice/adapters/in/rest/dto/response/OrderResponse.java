package com.hub.bookstoreorderservice.adapters.in.rest.dto.response;

import com.hub.bookstoreorderservice.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String userId,
        OrderStatus orderStatus,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Instant createdAt
) {}
