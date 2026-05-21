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
    void verify_invalidSignatureThrows() {
        assertThatThrownBy(() -> verifier.verify("0x0000000000000000000000000000000000000001",
                "msg", "0x" + "11".repeat(65), 1L))
                .isInstanceOf(BffException.class);
    }

    private String signMessage(String message, ECKeyPair keyPair) {
        String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
        byte[] prefixed = (prefix + message).getBytes(StandardCharsets.UTF_8);
        Sign.SignatureData sig = Sign.signPrefixedMessage(prefixed, keyPair);
        byte[] result = new byte[65];
        System.arraycopy(sig.getR(), 0, result, 0, 32);
        System.arraycopy(sig.getS(), 0, result, 32, 32);
        result[64] = sig.getV()[0];
        return Numeric.toHexString(result);
    }
}
