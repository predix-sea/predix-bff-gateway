package com.predix.bff.security;

import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SiweVerifier {

    private static final Pattern NONCE_PATTERN = Pattern.compile("Nonce:\\s*(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(?i)0x[a-fA-F0-9]{40}");

    public void verify(String walletAddress, String message, String signature, long chainId) {
        String recovered = recoverAddress(message, signature);
        if (!recovered.equalsIgnoreCase(walletAddress)) {
            throw new BffException(ErrorCode.AUTH_INVALID_SIGNATURE, "Wallet address does not match signature");
        }
        if (!message.toLowerCase().contains(walletAddress.toLowerCase())) {
            throw new BffException(ErrorCode.AUTH_INVALID_SIGNATURE, "Message does not contain wallet address");
        }
    }

    public String extractNonce(String message) {
        Matcher matcher = NONCE_PATTERN.matcher(message);
        if (!matcher.find()) {
            throw new BffException(ErrorCode.AUTH_INVALID_SIGNATURE, "Nonce not found in SIWE message");
        }
        return matcher.group(1).trim();
    }

    private String recoverAddress(String message, String signatureHex) {
        try {
            byte[] signatureBytes = Numeric.hexStringToByteArray(signatureHex.startsWith("0x") ? signatureHex : "0x" + signatureHex);
            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }
            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(messageBytes, signatureData);
            return "0x" + Keys.getAddress(publicKey);
        } catch (Exception e) {
            throw new BffException(ErrorCode.AUTH_INVALID_SIGNATURE, "Failed to verify signature");
        }
    }
}
