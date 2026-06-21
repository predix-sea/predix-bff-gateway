package com.predix.bff.security;

import com.predix.bff.exception.BffException;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiweVerifierTest {

    private final SiweVerifier verifier = new SiweVerifier();

    @Test
    void extractNonce_parsesMessage() {
        String nonce = verifier.extractNonce("Sign in\nNonce: test-nonce-123\n");
        assertThat(nonce).isEqualTo("test-nonce-123");
    }

    @Test
    void verify_validSignature() throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        String address = "0x" + Keys.getAddress(keyPair);
        String message = "Login for " + address + "\nNonce: abc123";
        String signature = signMessage(message, keyPair);

        verifier.verify(address, message, signature, 1L);
    }

    @Test
    void verify_metamaskStyleSiweMessage() {
        String message =
                """
                predix.local wants you to sign in with your Ethereum account:

                0x8363B9e48f4c13A5Dfab27fdE2E7dcf7C6aD671A

                Sign in to PrediX BFF Gateway

                URI: http://localhost:3001
                Version: 1
                Chain ID: 1
                Nonce: QfpziTmBFnEXiky9kNbWwPS6gpKC6sp2El-wcC-4xgI
                Issued At: 2026-06-12T13:16:03.471096Z
                """;
        String signature =
                "0x99b279fed81f28904ef4601972e2b7f39289029c625ad697798e5ab99b02a351538deae5fc37562c8bde09ed9a547317d729dbe5ff75ae01c25733a458990cde1b";

        verifier.verify("0x8363B9e48f4c13A5Dfab27fdE2E7dcf7C6aD671A", message, signature, 1L);
    }

    @Test
    void verify_invalidSignatureThrows() {
        assertThatThrownBy(() -> verifier.verify("0x0000000000000000000000000000000000000001",
                "msg", "0x" + "11".repeat(65), 1L))
                .isInstanceOf(BffException.class);
    }

    private String signMessage(String message, ECKeyPair keyPair) {
        Sign.SignatureData sig =
                Sign.signPrefixedMessage(message.getBytes(StandardCharsets.UTF_8), keyPair);
        byte[] result = new byte[65];
        System.arraycopy(sig.getR(), 0, result, 0, 32);
        System.arraycopy(sig.getS(), 0, result, 32, 32);
        result[64] = sig.getV()[0];
        return Numeric.toHexString(result);
    }
}
