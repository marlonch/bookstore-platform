package com.hub.adapters;

import com.hub.application.catalog.port.in.CreateBookUseCase;
import com.hub.application.catalog.port.in.ListBooksUseCase;
import com.hub.application.catalog.port.in.command.CreateBookCommand;
import com.hub.application.identity.port.in.CreateUserUseCase;
import com.hub.application.identity.port.in.ListUsersUseCase;
import com.hub.application.identity.port.in.command.CreateUserCommand;
import com.hub.domain.identity.Role;
import com.hub.domain.identity.exception.DuplicateEmailException;
import com.hub.domain.identity.exception.DuplicateUsernameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Seeds demo data on startup so the API is immediately usable after `docker compose up`.
 * Runs only in non-test profiles; idempotent — skips if data already exists.
 *
 * Default credentials:  admin / Admin123!   (ADMINISTRATOR role)
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final CreateUserUseCase createUserUseCase;
    private final CreateBookUseCase createBookUseCase;
    private final ListBooksUseCase listBooksUseCase;

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedBooks();
    }

    private void seedAdminUser() {
        try {
            createUserUseCase.createUser(new CreateUserCommand(
                    "admin",
                    "admin@hub.com",
                    "Admin123!",
                    Set.of(Role.ADMINISTRATOR)));
            log.info("[DataSeeder] Admin user created  ->  username=admin  password=Admin123!");
        } catch (DuplicateUsernameException | DuplicateEmailException e) {
            log.debug("[DataSeeder] Admin user already exists, skipping.");
        }
    }

    private void seedBooks() {
        if (!listBooksUseCase.listBooks().isEmpty()) {
            log.debug("[DataSeeder] Books already exist, skipping seed.");
            return;
        }

        List<CreateBookCommand> books = List.of(
                new CreateBookCommand("Clean Code", "Robert C. Martin", 2008),
                new CreateBookCommand("The Pragmatic Programmer", "David Thomas, Andrew Hunt", 1999),
                new CreateBookCommand("Domain-Driven Design", "Eric Evans", 2003),
                new CreateBookCommand("Designing Data-Intensive Applications", "Martin Kleppmann", 2017),
                new CreateBookCommand("Refactoring", "Martin Fowler", 1999)
        );

        books.forEach(cmd -> {
            createBookUseCase.createBook(cmd);
            log.info("[DataSeeder] Book seeded -> '{}'", cmd.title());
        });
    }
}
