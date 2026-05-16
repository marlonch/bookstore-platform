package com.hub.adapters.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter
@Setter
public class StockJpaEntity {
    @Id
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID bookId;

    private Integer quantity;
}
