package com.hub.bookstoreorderservice.adapters.out.persistence.jpa.entity;

import com.hub.bookstoreorderservice.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity representing an order record stored in MySQL.
 * Intentionally isolated from the domain model to prevent persistence
 * concerns from leaking into the core business layer.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID orderId;

    @Column(nullable = false, length = 255, updatable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderItemJpaEntity> items = new ArrayList<>();
}
