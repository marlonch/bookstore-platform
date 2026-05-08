package com.hub.application.catalog.port.in.command;

public record CreateBookCommand(String title, String author, Integer publishedYear) {}
