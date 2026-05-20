package com.hub.bookstoreorderservice.adapters.in.rest.controller;

import com.hub.bookstoreorderservice.adapters.in.rest.dto.request.CreateOrderRequest;
import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderResponse;
import com.hub.bookstoreorderservice.application.order.port.out.BookValidationPort;
import com.hub.bookstoreorderservice.application.order.port.out.dto.BookDetails;
import com.hub.bookstoreorderservice.domain.model.OrderStatus;
import com.hub.bookstoreorderservice.support.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderControllerIT {

    private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final BigDecimal PRICE = new BigDecimal("29.99");

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @MockitoBean
    private BookValidationPort bookValidationPort;

    private String userToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userToken = TestJwtFactory.token("alice", "ROLE_USER");
        otherToken = TestJwtFactory.token("bob", "ROLE_USER");
        when(bookValidationPort.getBook(any(UUID.class)))
                .thenReturn(new BookDetails(BOOK_ID, "Clean Code", PRICE));
    }

    // --- POST /api/orders ---

    @Test
    void createOrder_withValidToken_returns201AndOrder() {
        webTestClient.post().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateOrderRequest(BOOK_ID, 2))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .value(body -> {
                    assertThat(body.orderId()).isNotNull();
                    assertThat(body.userId()).isEqualTo("alice");
                    assertThat(body.orderStatus()).isEqualTo(OrderStatus.CREATED);
                    assertThat(body.items()).hasSize(1);
                    assertThat(body.items().get(0).quantity()).isEqualTo(2);
                    assertThat(body.totalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));
                });
    }

    @Test
    void createOrder_withoutToken_returns401() {
        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateOrderRequest(BOOK_ID, 1))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- GET /api/orders/{orderId} ---

    @Test
    void getOrder_withExistingOrder_returns200() {
        UUID orderId = createOrder();

        webTestClient.get().uri("/api/orders/" + orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(body -> assertThat(body.orderId()).isEqualTo(orderId));
    }

    @Test
    void getOrder_byDifferentUser_returns403() {
        UUID orderId = createOrder();

        webTestClient.get().uri("/api/orders/" + orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getOrder_withUnknownId_returns400() {
        webTestClient.get().uri("/api/orders/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- GET /api/orders ---

    @Test
    void listOrders_returnsOrdersForAuthenticatedUser() {
        createOrder();
        createOrder();

        webTestClient.get().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponse.class)
                .hasSize(2);
    }

    // --- DELETE /api/orders/{orderId} ---

    @Test
    void cancelOrder_withExistingOrder_returns204() {
        UUID orderId = createOrder();

        webTestClient.delete().uri("/api/orders/" + orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void cancelOrder_byDifferentUser_returns403() {
        UUID orderId = createOrder();

        webTestClient.delete().uri("/api/orders/" + orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void cancelOrder_withoutToken_returns401() {
        webTestClient.delete().uri("/api/orders/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- helpers ---

    private UUID createOrder() {
        return webTestClient.post().uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateOrderRequest(BOOK_ID, 1))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult()
                .getResponseBody()
                .orderId();
    }
}
