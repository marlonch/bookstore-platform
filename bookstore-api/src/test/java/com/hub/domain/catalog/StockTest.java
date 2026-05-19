package com.hub.domain.catalog;

import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.exception.InsufficientStockException;
import com.hub.domain.catalog.exception.InvalidBookException;
import com.hub.domain.catalog.stock.Stock;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    private static final BookId BOOK_ID = new BookId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    // --- createNew ---

    @Test
    void createNew_withPositiveQuantity_createsStock() {
        Stock stock = Stock.createNew(BOOK_ID, 10);
        assertThat(stock.getBookId()).isEqualTo(BOOK_ID);
        assertThat(stock.getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void createNew_withZeroQuantity_createsStockWithZero() {
        Stock stock = Stock.createNew(BOOK_ID, 0);
        assertThat(stock.getAvailableQuantity()).isEqualTo(0);
    }

    @Test
    void createNew_withNullBookId_throwsInvalidBookException() {
        assertThatThrownBy(() -> Stock.createNew(null, 5))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void createNew_withNegativeQuantity_throwsInvalidBookException() {
        assertThatThrownBy(() -> Stock.createNew(BOOK_ID, -1))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining(">= 0");
    }

    // --- existing ---

    @Test
    void existing_reconstructsStockWithCurrentQuantity() {
        Stock stock = Stock.existing(BOOK_ID, 7);
        assertThat(stock.getBookId()).isEqualTo(BOOK_ID);
        assertThat(stock.getAvailableQuantity()).isEqualTo(7);
    }

    @Test
    void existing_withNullBookId_throwsInvalidBookException() {
        assertThatThrownBy(() -> Stock.existing(null, 5))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void existing_withNegativeQuantity_throwsInvalidBookException() {
        assertThatThrownBy(() -> Stock.existing(BOOK_ID, -1))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining(">= 0");
    }

    // --- restock ---

    @Test
    void restock_addsToAvailableQuantity() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        stock.restock(3);
        assertThat(stock.getAvailableQuantity()).isEqualTo(8);
    }

    @Test
    void restock_withZero_throwsInvalidBookException() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        assertThatThrownBy(() -> stock.restock(0))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("> 0");
    }

    @Test
    void restock_withNegative_throwsInvalidBookException() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        assertThatThrownBy(() -> stock.restock(-2))
                .isInstanceOf(InvalidBookException.class);
    }

    // --- reserve ---

    @Test
    void reserve_decreasesAvailableQuantity() {
        Stock stock = Stock.createNew(BOOK_ID, 10);
        stock.reserve(3);
        assertThat(stock.getAvailableQuantity()).isEqualTo(7);
    }

    @Test
    void reserve_exactAvailableQuantity_succeeds() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        stock.reserve(5);
        assertThat(stock.getAvailableQuantity()).isEqualTo(0);
    }

    @Test
    void reserve_moreThanAvailable_throwsInsufficientStockException() {
        Stock stock = Stock.createNew(BOOK_ID, 3);
        assertThatThrownBy(() -> stock.reserve(4))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void reserve_onEmptyStock_throwsInsufficientStockException() {
        Stock stock = Stock.createNew(BOOK_ID, 0);
        assertThatThrownBy(() -> stock.reserve(1))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void reserve_withZero_throwsInvalidBookException() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        assertThatThrownBy(() -> stock.reserve(0))
                .isInstanceOf(InvalidBookException.class);
    }

    // --- release ---

    @Test
    void release_addsBackToAvailableQuantity() {
        Stock stock = Stock.createNew(BOOK_ID, 10);
        stock.reserve(4);
        stock.release(4);
        assertThat(stock.getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void release_withZero_throwsInvalidBookException() {
        Stock stock = Stock.createNew(BOOK_ID, 5);
        assertThatThrownBy(() -> stock.release(0))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("> 0");
    }
}
