package com.hub.bookstoreorderservice.adapters.in.rest.mapper;

import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderItemResponse;
import com.hub.bookstoreorderservice.adapters.in.rest.dto.response.OrderResponse;
import com.hub.bookstoreorderservice.domain.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderRestMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getBookId(), i.getQuantity(), i.getUnitPrice(), i.subtotal()))
                .toList();
        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderStatus(),
                items,
                order.totalAmount(),
                order.getCreatedAt());
    }
}
