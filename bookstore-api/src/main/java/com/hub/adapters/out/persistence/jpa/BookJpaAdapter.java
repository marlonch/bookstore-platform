package com.hub.adapters.out.persistence.jpa;

import com.hub.adapters.out.persistence.jpa.entity.UserJpaEntity;
import com.hub.adapters.out.persistence.jpa.mapper.BookJpaMapper;
import com.hub.adapters.out.persistence.jpa.repository.BookJpaRepository;
import com.hub.adapters.out.persistence.jpa.repository.UserJpaRepository;
import com.hub.application.catalog.book.port.out.BookRepositoryPort;
import com.hub.domain.catalog.book.Book;
import com.hub.domain.catalog.book.BookId;
import com.hub.domain.catalog.book.ISBN;
import com.hub.domain.identity.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
            owner = userJpaRepository.findById(book.getOwnerId().get().value()).orElse(null);
        }
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(book, owner)));
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Book> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Book> findByOwnerId(UserId ownerId) {
        return jpaRepository.findByOwner_Id(ownerId.value()).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(BookId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(BookId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public boolean existsByIsbn(ISBN isbn) {
        return jpaRepository.existsByIsbn(isbn.getValue());
    }
}
