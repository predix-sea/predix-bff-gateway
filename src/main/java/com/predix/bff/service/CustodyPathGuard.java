package com.predix.bff.service;

import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Ensures fund-related operations only route through BACP client.
 */
@Component
public class CustodyPathGuard {

    private static final ThreadLocal<Boolean> BACP_CONTEXT = ThreadLocal.withInitial(() -> false);

    public void enterBacpContext(Runnable action) {
        boolean previous = BACP_CONTEXT.get();
        BACP_CONTEXT.set(true);
        try {
            action.run();
        } finally {
            BACP_CONTEXT.set(previous);
        }
    }

    public <T> T executeInBacpContext(java.util.function.Supplier<T> supplier) {
        boolean previous = BACP_CONTEXT.get();
        BACP_CONTEXT.set(true);
        try {
            return supplier.get();
        } finally {
            BACP_CONTEXT.set(previous);
        }
    }

    public static void assertBacpOnly(String operation) {
        if (!Boolean.TRUE.equals(BACP_CONTEXT.get())) {
            throw new BffException(ErrorCode.CUSTODY_PATH_VIOLATION,
                    "Fund operation '" + operation + "' must use BACP client only");
        }
    }

    public static boolean isInBacpContext() {
        return Boolean.TRUE.equals(BACP_CONTEXT.get());
    }
}
