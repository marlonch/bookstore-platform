package com.hub.bookstoreorderservice.domain.model;

import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import com.hub.bookstoreorderservice.domain.exception.InvalidQuantityException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class OrderItem {

    private final UUID bookId;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(UUID bookId, int quantity, BigDecimal unitPrice) {
        if (bookId == null) {
            throw new InvalidOrderException("bookId must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("unitPrice must be greater than zero");
        }
        this.bookId = bookId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem that)) return false;
        return quantity == that.quantity
                && Objects.equals(bookId, that.bookId)
                && unitPrice.compareTo(that.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, quantity, unitPrice.stripTrailingZeros());
    }
}
