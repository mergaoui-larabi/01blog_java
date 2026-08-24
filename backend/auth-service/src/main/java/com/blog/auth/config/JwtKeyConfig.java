package com.blog.auth.config;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtKeyConfig {

    @Bean
    RSAPrivateKey rsaPrivateKey(JwtProperties jwtProperties) throws Exception {
        try (Reader reader = Files.newBufferedReader(
                Path.of(jwtProperties.privateKeyPath()))) {

            PEMParser parser = new PEMParser(reader);
            Object object = parser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            return (RSAPrivateKey) converter.getPrivateKey(
                    ((PrivateKeyInfo) object));
        }
    }

    @Bean
    RSAPublicKey rsaPublicKey(JwtProperties jwtProperties) throws Exception {
        Path path = Path.of(jwtProperties.publicKeyPath());

        try (
                Reader reader = Files.newBufferedReader(path);
                PEMParser parser = new PEMParser(reader)) {
            Object object = parser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            return (RSAPublicKey) converter.getPublicKey(
                    (SubjectPublicKeyInfo) object);
        }
    }
}
