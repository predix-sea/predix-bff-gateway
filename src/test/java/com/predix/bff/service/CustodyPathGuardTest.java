package com.predix.bff.service;

import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustodyPathGuardTest {

    private final CustodyPathGuard guard = new CustodyPathGuard();

    @Test
    void blocksFundOpsOutsideBacpContext() {
        assertThatThrownBy(() -> CustodyPathGuard.assertBacpOnly("deposit"))
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.CUSTODY_PATH_VIOLATION);
    }

    @Test
    void allowsInsideBacpContext() {
        String result = guard.executeInBacpContext(() -> {
            CustodyPathGuard.assertBacpOnly("deposit");
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
    }
}
