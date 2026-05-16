package com.hub.adapters.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter
@Setter
public class StockJpaEntity {

    @Id
    @Column(name = "book_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_stock_book"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BookJpaEntity book;

    private Integer quantity;
}
