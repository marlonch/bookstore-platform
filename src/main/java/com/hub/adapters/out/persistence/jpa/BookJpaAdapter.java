package com.hub.adapters.out.persistence.jpa;

import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.adapters.out.persistence.jpa.mapper.BookJpaMapper;
import com.hub.adapters.out.persistence.jpa.repository.BookJpaRepository;
import com.hub.adapters.out.persistence.jpa.repository.UserJpaRepository;
import com.hub.application.catalog.port.out.BookRepositoryPort;
import com.hub.domain.catalog.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookJpaAdapter implements BookRepositoryPort {

    private final BookJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaMapper mapper;

    @Override
    public Book save(Book book) {
        UserJpaEntity owner = null;
        if (book.getOwnerId().isPresent()) {
            owner = userJpaRepository.findById(book.getOwnerId().get()).orElse(null);
        }
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(book, owner)));
    }

    @Override
    public Optional<Book> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Book> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Book> findByOwnerId(Long ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
