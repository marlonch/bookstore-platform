package com.hub.app.service;

import com.hub.application.auth.port.out.TokenMetadataRepositoryPort;
import com.hub.application.catalog.book.port.in.CreateBookUseCase;
import com.hub.application.catalog.book.port.in.command.CreateBookCommand;
import com.hub.application.catalog.stock.port.in.ReleaseStockUseCase;
import com.hub.application.catalog.stock.port.in.ReserveStockUseCase;
import com.hub.application.catalog.stock.port.in.RestockUseCase;
import com.hub.application.catalog.stock.port.in.command.ReleaseStockCommand;
import com.hub.application.catalog.stock.port.in.command.ReserveStockCommand;
import com.hub.application.catalog.stock.port.in.command.RestockCommand;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.catalog.exception.InsufficientStockException;
import com.hub.domain.catalog.exception.StockNotFoundException;
import com.hub.domain.catalog.stock.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StockServiceIT {

    private static final ISBN TEST_ISBN = new ISBN("9780134190440");

    @MockitoBean
    TokenMetadataRepositoryPort tokenMetadataRepositoryPort;

    @Autowired CreateBookUseCase createBookUseCase;
    @Autowired RestockUseCase restockUseCase;
    @Autowired ReserveStockUseCase reserveStockUseCase;
    @Autowired ReleaseStockUseCase releaseStockUseCase;

    private Book bookWithStock(int initialStock) {
        return createBookUseCase.createBook(new CreateBookCommand(
                "Test Book", "Test Author", 2024, new BigDecimal("29.99"), TEST_ISBN, initialStock));
    }

    @Test
    void createBook_persistsStockWithInitialQuantity() {
        Book book = bookWithStock(5);
        Stock afterReserve = reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 3));
        assertThat(afterReserve.getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void restock_increasesAvailableQuantity() {
        Book book = bookWithStock(5);
        Stock result = restockUseCase.restock(new RestockCommand(book.getId(), 10));
        assertThat(result.getAvailableQuantity()).isEqualTo(15);
    }

    @Test
    void reserve_decreasesAvailableQuantity() {
        Book book = bookWithStock(10);
        Stock result = reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 3));
        assertThat(result.getAvailableQuantity()).isEqualTo(7);
    }

    @Test
    void release_increasesAvailableQuantity() {
        Book book = bookWithStock(10);
        reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 4));
        Stock result = releaseStockUseCase.release(new ReleaseStockCommand(book.getId(), 2));
        assertThat(result.getAvailableQuantity()).isEqualTo(8);
    }

    @Test
    void reserve_exactAvailableQuantity_dropsToZero() {
        Book book = bookWithStock(5);
        Stock result = reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 5));
        assertThat(result.getAvailableQuantity()).isEqualTo(0);
    }

    @Test
    void reserve_withInsufficientStock_throwsInsufficientStockException() {
        Book book = bookWithStock(3);
        assertThatThrownBy(() -> reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 10)))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void restock_afterFullReservation_restoresCapacity() {
        Book book = bookWithStock(2);
        reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 2));
        restockUseCase.restock(new RestockCommand(book.getId(), 5));
        Stock result = reserveStockUseCase.reserve(new ReserveStockCommand(book.getId(), 3));
        assertThat(result.getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void operations_onNonExistentBook_throwsStockNotFoundException() {
        BookId missing = new BookId(UUID.randomUUID());
        assertThatThrownBy(() -> restockUseCase.restock(new RestockCommand(missing, 5)))
                .isInstanceOf(StockNotFoundException.class);
    }
}
