package com.gopoli.api.controller;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

final class ApiResponses {

    private static final Logger log = LoggerFactory.getLogger(ApiResponses.class);

    static final String INVALID_TOKEN = "Token inválido o ausente";

    private ApiResponses() {
    }

    static ResponseEntity<Object> unauthorized() {
        return status(401, INVALID_TOKEN);
    }

    static ResponseEntity<Object> status(int status, Object body) {
        return ResponseEntity.status(status).body(body);
    }

    static ResponseEntity<?> guarded(String failureMessage, Supplier<ResponseEntity<?>> action) {
        try {
            return action.get();
        } catch (RuntimeException e) {
            log.error("{}: {}", failureMessage, e.getMessage(), e);
            return status(500, failureMessage);
        }
    }
}
