package com.hub.adapters.out.persistence.jpa.repository;

import com.hub.adapters.out.persistence.jpa.entity.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long> {

    List<BookJpaEntity> findByOwnerId(Long ownerId);
}
