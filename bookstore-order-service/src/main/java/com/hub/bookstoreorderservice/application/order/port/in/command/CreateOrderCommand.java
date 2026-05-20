package com.hub.bookstoreorderservice.application.order.port.in.command;

/**
 * Command carrying the data required to create a new order.
 * {@code userId} is injected by the controller from the security context,
 * never trusted from the request body.
 */

import java.util.UUID;

public record CreateOrderCommand(UUID bookId, String userId, int quantity) {}
