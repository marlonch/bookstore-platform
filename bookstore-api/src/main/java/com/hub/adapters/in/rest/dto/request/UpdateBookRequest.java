package com.hub.adapters.in.rest.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBookRequest(
        @Size(max = 300) String title,
        @Size(max = 200) String author,
        Integer publishedYear,
        @Positive BigDecimal price
) {}