package com.blog.auth.security;

import java.security.interfaces.RSAPublicKey;

import org.springframework.stereotype.Service;

import com.blog.auth.config.JwtProperties;
import com.nimbusds.jose.jwk.RSAKey;

@Service
public class JwksService {

    private final RSAKey publicJwk;

    public JwksService(
            RSAPublicKey rsaPublicKey,
            JwtProperties jwtProperties) {

        this.publicJwk = new RSAKey.Builder(rsaPublicKey)
                .keyID(jwtProperties.keyId())
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .build();
    }

    public RSAKey getPublicJwk() {
        return publicJwk;
    }
}