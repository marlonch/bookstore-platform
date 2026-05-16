package com.hub.application.catalog.port.in.command;

import com.hub.domain.catalog.book.ISBN;

import java.math.BigDecimal;

public record CreateBookCommand(String title, String author, Integer publishedYear, BigDecimal price, ISBN isbn) {}