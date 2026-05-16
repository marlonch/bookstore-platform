package com.hub.adapters.out.persistence.jpa.repository;

import com.hub.adapters.out.persistence.jpa.entity.StockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockJpaRepository extends JpaRepository<StockJpaEntity, UUID> {
}