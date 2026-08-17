package com.ciphermarket.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

public final class ProblemFactory {

    private ProblemFactory() {
    }

    public static ProblemDetail create(
            HttpStatus status,
            String title,
            String detail,
            String correlationId
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://ciphermarket.local/problems/" + status.value()));
        problem.setProperty("correlationId", correlationId);
        return problem;
    }
}
