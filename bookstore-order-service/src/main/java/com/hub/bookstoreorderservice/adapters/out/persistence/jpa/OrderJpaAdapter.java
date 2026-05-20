package com.hub.bookstoreorderservice.adapters.out.persistence.jpa;

import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.mapper.OrderJpaMapper;
import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.repository.OrderJpaRepository;
import com.hub.bookstoreorderservice.application.order.port.out.OrderRepositoryPort;
import com.hub.bookstoreorderservice.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA outbound adapter implementing {@link OrderRepositoryPort}.
 * Translates between domain {@link Order} aggregates and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderJpaMapper orderJpaMapper;

    @Override
    public Order save(Order order) {
        return orderJpaMapper.toDomain(orderJpaRepository.save(orderJpaMapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId).map(orderJpaMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(String userId) {
        return orderJpaRepository.findByUserId(userId).stream()
                .map(orderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID orderId) {
        orderJpaRepository.deleteById(orderId);
    }
}
