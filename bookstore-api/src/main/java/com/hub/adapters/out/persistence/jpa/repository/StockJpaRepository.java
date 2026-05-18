package com.hub.adapters.out.persistence.jpa.repository;

import com.hub.adapters.out.persistence.jpa.entity.StockJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StockJpaRepository extends JpaRepository<StockJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockJpaEntity s WHERE s.bookId = :bookId")
    Optional<StockJpaEntity> findByBookIdForUpdate(UUID bookId);
}
