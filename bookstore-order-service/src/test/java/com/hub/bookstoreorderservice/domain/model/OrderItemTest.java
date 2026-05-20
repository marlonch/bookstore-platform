package com.hub.bookstoreorderservice.domain.model;

import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import com.hub.bookstoreorderservice.domain.exception.InvalidQuantityException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final BigDecimal UNIT_PRICE = new BigDecimal("29.99");

    @Test
    void constructor_withValidData_setsFields() {
        OrderItem item = new OrderItem(BOOK_ID, 2, UNIT_PRICE);

        assertThat(item.getBookId()).isEqualTo(BOOK_ID);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice()).isEqualByComparingTo(UNIT_PRICE);
    }

    @Test
    void subtotal_returnsQuantityTimesUnitPrice() {
        OrderItem item = new OrderItem(BOOK_ID, 3, new BigDecimal("10.00"));
        assertThat(item.subtotal()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void subtotal_withDecimalPrice_isAccurate() {
        OrderItem item = new OrderItem(BOOK_ID, 2, new BigDecimal("29.99"));
        assertThat(item.subtotal()).isEqualByComparingTo(new BigDecimal("59.98"));
    }

    @Test
    void constructor_withNullBookId_throwsInvalidOrderException() {
        assertThatThrownBy(() -> new OrderItem(null, 1, UNIT_PRICE))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void constructor_withZeroQuantity_throwsInvalidQuantityException() {
        assertThatThrownBy(() -> new OrderItem(BOOK_ID, 0, UNIT_PRICE))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    void constructor_withNegativeQuantity_throwsInvalidQuantityException() {
        assertThatThrownBy(() -> new OrderItem(BOOK_ID, -1, UNIT_PRICE))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    void constructor_withZeroUnitPrice_throwsInvalidOrderException() {
        assertThatThrownBy(() -> new OrderItem(BOOK_ID, 1, BigDecimal.ZERO))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void constructor_withNegativeUnitPrice_throwsInvalidOrderException() {
        assertThatThrownBy(() -> new OrderItem(BOOK_ID, 1, new BigDecimal("-5.00")))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void equals_sameValues_returnsTrue() {
        OrderItem a = new OrderItem(BOOK_ID, 2, UNIT_PRICE);
        OrderItem b = new OrderItem(BOOK_ID, 2, UNIT_PRICE);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_differentQuantity_returnsFalse() {
        OrderItem a = new OrderItem(BOOK_ID, 2, UNIT_PRICE);
        OrderItem b = new OrderItem(BOOK_ID, 3, UNIT_PRICE);
        assertThat(a).isNotEqualTo(b);
    }
}
