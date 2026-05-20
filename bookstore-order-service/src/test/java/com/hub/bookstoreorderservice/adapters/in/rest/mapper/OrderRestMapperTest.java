package com.hub.bookstoreorderservice.adapters.in.rest.mapper;

import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderItemResponse;
import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderResponse;
import com.hub.bookstoreorderservice.domain.model.Order;
import com.hub.bookstoreorderservice.domain.model.OrderItem;
import com.hub.bookstoreorderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRestMapperTest {

    private static final UUID BOOK_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOOK_B = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private OrderRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderRestMapper();
    }

    @Test
    void toResponse_mapsOrderHeaderFields() {
        Order order = Order.createNew(
                List.of(new OrderItem(BOOK_A, 1, new BigDecimal("10.00"))),
                "alice");

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.orderId()).isEqualTo(order.getOrderId());
        assertThat(response.userId()).isEqualTo("alice");
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    void toResponse_singleItem_mapsItemFields() {
        Order order = Order.createNew(
                List.of(new OrderItem(BOOK_A, 3, new BigDecimal("5.00"))),
                "alice");

        OrderItemResponse item = mapper.toResponse(order).items().get(0);

        assertThat(item.bookId()).isEqualTo(BOOK_A);
        assertThat(item.quantity()).isEqualTo(3);
        assertThat(item.unitPrice()).isEqualByComparingTo("5.00");
        assertThat(item.subtotal()).isEqualByComparingTo("15.00");
    }

    @Test
    void toResponse_multipleItems_allItemsMapped() {
        Order order = Order.createNew(List.of(
                new OrderItem(BOOK_A, 2, new BigDecimal("10.00")),
                new OrderItem(BOOK_B, 1, new BigDecimal("25.00"))), "bob");

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items())
                .extracting(OrderItemResponse::bookId)
                .containsExactly(BOOK_A, BOOK_B);
    }

    @Test
    void toResponse_multipleItems_totalAmountIsSumOfSubtotals() {
        Order order = Order.createNew(List.of(
                new OrderItem(BOOK_A, 2, new BigDecimal("10.00")),  // 20.00
                new OrderItem(BOOK_B, 3, new BigDecimal("5.00"))),  // 15.00
                "bob");

        assertThat(mapper.toResponse(order).totalAmount()).isEqualByComparingTo("35.00");
    }

    @Test
    void toResponse_itemSubtotal_isUnitPriceTimesQuantity() {
        Order order = Order.createNew(
                List.of(new OrderItem(BOOK_A, 4, new BigDecimal("12.50"))),
                "alice");

        OrderItemResponse item = mapper.toResponse(order).items().get(0);

        assertThat(item.subtotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void toResponse_preservesItemOrder() {
        Order order = Order.createNew(List.of(
                new OrderItem(BOOK_A, 1, new BigDecimal("1.00")),
                new OrderItem(BOOK_B, 1, new BigDecimal("2.00"))), "alice");

        List<OrderItemResponse> items = mapper.toResponse(order).items();

        assertThat(items.get(0).bookId()).isEqualTo(BOOK_A);
        assertThat(items.get(1).bookId()).isEqualTo(BOOK_B);
    }
}
