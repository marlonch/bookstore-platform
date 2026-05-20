package com.hub.bookstoreorderservice.adapters.in.rest.controller;

import com.hub.bookstoreorderservice.adapters.in.rest.dto.request.CreateOrderRequest;
import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderResponse;
import com.hub.bookstoreorderservice.adapters.in.rest.mapper.OrderRestMapper;
import com.hub.bookstoreorderservice.adapters.security.UserDetailsImpl;
import com.hub.bookstoreorderservice.application.order.port.in.*;
import com.hub.bookstoreorderservice.application.order.port.in.command.CreateOrderCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final OrderRestMapper orderMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        CreateOrderCommand command = new CreateOrderCommand(
                request.bookId(),
                principal.getUsername(),
                request.quantity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toResponse(createOrderUseCase.createOrder(command)));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(
                orderMapper.toResponse(getOrderUseCase.getOrder(orderId, principal.getUsername())));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @AuthenticationPrincipal UserDetailsImpl principal) {

        return ResponseEntity.ok(listOrdersUseCase.listOrders(principal.getUsername()).stream()
                .map(orderMapper::toResponse)
                .toList());
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        cancelOrderUseCase.cancelOrder(orderId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
