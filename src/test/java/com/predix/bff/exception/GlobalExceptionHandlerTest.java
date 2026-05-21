package com.predix.bff.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesBffException() {
        ResponseEntity<?> response = handler.handleBffException(new BffException(ErrorCode.COMPLIANCE_CN_BLOCKED));
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }
}
