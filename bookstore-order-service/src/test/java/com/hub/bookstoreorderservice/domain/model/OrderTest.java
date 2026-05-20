package com.hub.bookstoreorderservice.domain.model;

import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final String USER_ID = "alice";
    private static final UUID BOOK_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOOK_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final OrderItem ITEM_A = new OrderItem(BOOK_A, 2, new BigDecimal("29.99"));
    private static final OrderItem ITEM_B = new OrderItem(BOOK_B, 1, new BigDecimal("44.99"));

    // --- createNew ---

    @Test
    void createNew_withValidItems_setsCreatedStatusAndGeneratesId() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);

        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getUserId()).isEqualTo(USER_ID);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    void createNew_eachCall_generatesUniqueOrderId() {
        Order a = Order.createNew(List.of(ITEM_A), USER_ID);
        Order b = Order.createNew(List.of(ITEM_A), USER_ID);
        assertThat(a.getOrderId()).isNotEqualTo(b.getOrderId());
    }

    @Test
    void createNew_withMultipleItems_storesAll() {
        Order order = Order.createNew(List.of(ITEM_A, ITEM_B), USER_ID);
        assertThat(order.getItems()).containsExactly(ITEM_A, ITEM_B);
    }

    @Test
    void totalAmount_sumsAllItemSubtotals() {
        Order order = Order.createNew(List.of(ITEM_A, ITEM_B), USER_ID);
        // ITEM_A: 2 × 29.99 = 59.98 | ITEM_B: 1 × 44.99 = 44.99 | total = 104.97
        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("104.97"));
    }

    @Test
    void createNew_withEmptyItems_throwsInvalidOrderException() {
        assertThatThrownBy(() -> Order.createNew(List.of(), USER_ID))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void createNew_withNullItems_throwsInvalidOrderException() {
        assertThatThrownBy(() -> Order.createNew(null, USER_ID))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void createNew_withNullUserId_throwsInvalidOrderException() {
        assertThatThrownBy(() -> Order.createNew(List.of(ITEM_A), null))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void createNew_withBlankUserId_throwsInvalidOrderException() {
        assertThatThrownBy(() -> Order.createNew(List.of(ITEM_A), "   "))
                .isInstanceOf(InvalidOrderException.class);
    }

    // --- confirm ---

    @Test
    void confirm_fromCreated_setsConfirmed() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void confirm_fromConfirmed_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        assertThatThrownBy(order::confirm)
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void confirm_fromCancelled_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.cancel();
        assertThatThrownBy(order::confirm)
                .isInstanceOf(InvalidOrderException.class);
    }

    // --- ship ---

    @Test
    void ship_fromConfirmed_setsShipped() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        order.ship();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void ship_fromCreated_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        assertThatThrownBy(order::ship)
                .isInstanceOf(InvalidOrderException.class);
    }

    // --- deliver ---

    @Test
    void deliver_fromShipped_setsDelivered() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        order.ship();
        order.deliver();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void deliver_fromConfirmed_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        assertThatThrownBy(order::deliver)
                .isInstanceOf(InvalidOrderException.class);
    }

    // --- cancel ---

    @Test
    void cancel_fromCreated_setsCancelled() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.cancel();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_fromConfirmed_setsCancelled() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        order.cancel();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_fromShipped_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        order.ship();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("shipped or delivered");
    }

    @Test
    void cancel_fromDelivered_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.confirm();
        order.ship();
        order.deliver();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("shipped or delivered");
    }

    @Test
    void cancel_whenAlreadyCancelled_throwsInvalidOrderException() {
        Order order = Order.createNew(List.of(ITEM_A), USER_ID);
        order.cancel();
        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("already cancelled");
    }
}
