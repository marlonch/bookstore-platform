package com.hub.bookstoreorderservice.adapters.out.persistence.jpa.repository;

import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    List<OrderJpaEntity> findByUserId(String userId);
}
