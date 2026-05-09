package com.hub.adapters.in.rest.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateBookRequest(
        @Size(max = 300) String title,
        @Size(max = 200) String author,
        Integer publishedYear
) {}
