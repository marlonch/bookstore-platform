package com.hub.bookstoreorderservice.adapters.in.rest.exception;

import com.hub.bookstoreorderservice.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomain_returnsProblemDetailWithUnprocessableContent() {
        DomainException ex = new DomainException("domain error") {};

        ProblemDetail result = handler.handleDomain(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
        assertThat(result.getDetail()).isEqualTo("domain error");
    }
}