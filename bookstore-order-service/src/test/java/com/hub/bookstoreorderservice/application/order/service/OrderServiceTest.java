package com.hub.bookstoreorderservice.application.order.service;

import com.hub.bookstoreorderservice.application.order.port.in.command.CreateOrderCommand;
import com.hub.bookstoreorderservice.application.order.port.out.BookValidationPort;
import com.hub.bookstoreorderservice.application.order.port.out.OrderRepositoryPort;
import com.hub.bookstoreorderservice.application.order.port.out.dto.BookDetails;
import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import com.hub.bookstoreorderservice.domain.model.Order;
import com.hub.bookstoreorderservice.domain.model.OrderItem;
import com.hub.bookstoreorderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final BigDecimal PRICE = new BigDecimal("29.99");
    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock OrderRepositoryPort orderRepository;
    @Mock BookValidationPort bookValidation;

    @InjectMocks
    OrderService orderService;

    // --- createOrder ---

    @Test
    void createOrder_fetchesPriceFromCatalogAndPersistsOrder() {
        BookDetails book = new BookDetails(BOOK_ID, "Clean Code", PRICE);
        when(bookValidation.getBook(BOOK_ID)).thenReturn(book);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(new CreateOrderCommand(BOOK_ID, "alice", 2));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getBookId()).isEqualTo(BOOK_ID);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo(PRICE);
        assertThat(result.getUserId()).isEqualTo("alice");
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        verify(bookValidation).getBook(BOOK_ID);
        verify(orderRepository).save(any());
    }

    @Test
    void createOrder_withNullCommand_throwsNullPointerException() {
        assertThatThrownBy(() -> orderService.createOrder(null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- getOrder ---

    @Test
    void getOrder_withExistingIdAndOwner_returnsOrder() {
        UUID id = UUID.randomUUID();
        Order order = Order.createNew(List.of(new OrderItem(BOOK_ID, 1, PRICE)), "alice");
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        Order result = orderService.getOrder(id, "alice");

        assertThat(result).isSameAs(order);
    }

    @Test
    void getOrder_byDifferentUser_throwsAccessDeniedException() {
        UUID id = UUID.randomUUID();
        Order order = Order.createNew(List.of(new OrderItem(BOOK_ID, 1, PRICE)), "alice");
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrder(id, "bob"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getOrder_withMissingId_throwsInvalidOrderException() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(id, "alice"))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining(id.toString());
    }

    // --- listOrders ---

    @Test
    void listOrders_returnsAllOrdersForUser() {
        UUID bookId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        List<Order> orders = List.of(
                Order.createNew(List.of(new OrderItem(BOOK_ID, 1, PRICE)), "alice"),
                Order.createNew(List.of(new OrderItem(bookId2, 3, PRICE)), "alice"));
        when(orderRepository.findByUserId("alice")).thenReturn(orders);

        assertThat(orderService.listOrders("alice")).hasSize(2);
    }

    @Test
    void listOrders_withNullUserId_throwsNullPointerException() {
        assertThatThrownBy(() -> orderService.listOrders(null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- cancelOrder ---

    @Test
    void cancelOrder_transitionsStatusToCancelledAndPersists() {
        UUID id = UUID.randomUUID();
        Order order = Order.existing(id,
                List.of(new OrderItem(BOOK_ID, 1, PRICE)),
                "alice", OrderStatus.CREATED, Instant.now());
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelOrder(id, "alice");

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_byDifferentUser_throwsAccessDeniedException() {
        UUID id = UUID.randomUUID();
        Order order = Order.existing(id,
                List.of(new OrderItem(BOOK_ID, 1, PRICE)),
                "alice", OrderStatus.CREATED, Instant.now());
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(id, "bob"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelOrder_withMissingId_throwsInvalidOrderException() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(id, "alice"))
                .isInstanceOf(InvalidOrderException.class);
    }
}
