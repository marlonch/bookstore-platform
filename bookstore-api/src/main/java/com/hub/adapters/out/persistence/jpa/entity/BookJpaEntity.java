package com.hub.adapters.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA persistence entity representing a book record stored in MySQL.
 *
 * This entity is intentionally isolated from the domain model to avoid
 * persistence concerns leaking into the core business layer.
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(name = "published_year")
    private Integer publishedYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserJpaEntity owner;
}
