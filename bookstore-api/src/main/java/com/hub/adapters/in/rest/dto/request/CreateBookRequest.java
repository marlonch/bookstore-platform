package com.hub.adapters.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 200) String author,
        Integer publishedYear
) {}
