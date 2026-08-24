
package com.blog.auth.security;

import java.util.Base64;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.blog.auth.config.JwtProperties;
import com.blog.auth.constant.JwtConstants;

@Service
public class JwtService {

    private final RSAPrivateKey rsaPrivateKey;
    private final JwtProperties jwtProperties;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public JwtService(RSAPrivateKey rsaPrivateKey, JwtProperties jwtProperties) {
        this.rsaPrivateKey = rsaPrivateKey;
        this.jwtProperties = jwtProperties;
    }

    private String encode(String value) {
        return encoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String input) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(rsaPrivateKey);
            signature.update(input.getBytes(StandardCharsets.UTF_8));
            return encoder.encodeToString(signature.sign());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }

    // create jwt token

    public String createToken(UUID userId, String role) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(jwtProperties.expiration());

        String header = JwtConstants.HEADER_TEMPLATE.formatted(jwtProperties.keyId());

        String payload = JwtConstants.PAYLOAD_TEMPLATE.formatted(
                userId,
                jwtProperties.issuer(),
                role,
                issuedAt.getEpochSecond(),
                expiration.getEpochSecond());

        String encodedHeader = encode(header);
        String encodedPayload = encode(payload);
        String signingInput = encodedHeader + "." + encodedPayload;
        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }
}