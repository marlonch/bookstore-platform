package com.hub.bookstoreorderservice.application.order.service;

import com.hub.bookstoreorderservice.application.order.port.in.*;
import com.hub.bookstoreorderservice.application.order.port.in.command.CreateOrderCommand;
import com.hub.bookstoreorderservice.application.order.port.out.BookValidationPort;
import com.hub.bookstoreorderservice.application.order.port.out.OrderRepositoryPort;
import com.hub.bookstoreorderservice.application.order.port.out.dto.BookDetails;
import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import com.hub.bookstoreorderservice.domain.model.Order;
import com.hub.bookstoreorderservice.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase, GetOrderUseCase, ListOrdersUseCase, CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final BookValidationPort bookValidation;

    @Override
    public Order createOrder(CreateOrderCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        BookDetails book = bookValidation.getBook(command.bookId());
        OrderItem item = new OrderItem(book.id(), command.quantity(), book.price());
        Order order = Order.createNew(List.of(item), command.userId());
        return orderRepository.save(order);
    }

    @Override
    public Order getOrder(UUID orderId, String requestingUserId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(requestingUserId, "requestingUserId must not be null");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order not found: " + orderId));
        if (!order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Access denied");
        }
        return order;
    }

    @Override
    public List<Order> listOrders(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return orderRepository.findByUserId(userId);
    }

    @Override
    public void cancelOrder(UUID orderId, String requestingUserId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(requestingUserId, "requestingUserId must not be null");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order not found: " + orderId));
        if (!order.getUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Access denied");
        }
        order.cancel();
        orderRepository.save(order);
    }
}
