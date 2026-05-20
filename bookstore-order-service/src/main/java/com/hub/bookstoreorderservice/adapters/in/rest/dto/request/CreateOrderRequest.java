package com.hub.bookstoreorderservice.adapters.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID bookId,
        @Positive int quantity
) {}
