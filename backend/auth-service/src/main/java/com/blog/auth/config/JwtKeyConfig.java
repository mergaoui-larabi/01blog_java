package com.blog.auth.config;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtKeyConfig {

    @Bean
    RSAPrivateKey rsaPrivateKey(JwtProperties properties) throws Exception {
        try (Reader reader = Files.newBufferedReader(
                Path.of(properties.privateKeyPath()))) {

            PEMParser parser = new PEMParser(reader);
            Object object = parser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            return (RSAPrivateKey) converter.getPrivateKey(
                    ((PrivateKeyInfo) object));
        }
    }
}
