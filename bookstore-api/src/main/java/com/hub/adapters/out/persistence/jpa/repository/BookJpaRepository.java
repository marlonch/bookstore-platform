package com.hub.adapters.out.persistence.jpa.repository;

import com.hub.adapters.out.persistence.jpa.entity.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    List<BookJpaEntity> findByOwner_Id(UUID ownerId);

    boolean existsByIsbn(String isbn);
}
