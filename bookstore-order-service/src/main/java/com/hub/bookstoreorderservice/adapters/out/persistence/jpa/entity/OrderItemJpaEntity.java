package com.hub.bookstoreorderservice.adapters.out.persistence.jpa.entity;

import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.converter.UuidBinaryConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private OrderJpaEntity order;

    @Convert(converter = UuidBinaryConverter.class)
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID bookId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal unitPrice;
}
