package com.hub.adapters.in.rest.dto.response;

public record BookResponse(Long id, String title, String author, Integer publishedYear, Long ownerId) {}
