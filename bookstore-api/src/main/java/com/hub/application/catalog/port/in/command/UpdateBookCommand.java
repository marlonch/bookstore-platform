package com.hub.application.catalog.port.in.command;

public record UpdateBookCommand(Long id, String title, String author, Integer publishedYear) {}
