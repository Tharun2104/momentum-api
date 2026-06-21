package com.mttauto.momentum_api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long ttlSeconds;

    public JwtService(
            @Value("${app.jwt.secret:momentum-local-dev-secret-change-me}") String secret,
            @Value("${app.jwt.ttl-seconds:86400}") long ttlSeconds
    ) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(Long userId) {
        long expiresAt = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + userId + "\",\"exp\":" + expiresAt + "}");
        String content = header + "." + payload;
        return content + "." + sign(content);
    }

    public Long validateAndGetUserId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new AuthenticationException("Invalid token");
        }

        String content = parts[0] + "." + parts[1];
        if (!MessageDigestSafeEquals.equals(sign(content), parts[2])) {
            throw new AuthenticationException("Invalid token");
        }

        String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        long expiresAt = Long.parseLong(value(payload, "\"exp\":", "}"));
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new AuthenticationException("Token expired");
        }

        return Long.parseLong(value(payload, "\"sub\":\"", "\""));
    }

    private String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private String value(String source, String prefix, String suffix) {
        int start = source.indexOf(prefix);
        if (start < 0) {
            throw new AuthenticationException("Invalid token");
        }
        int valueStart = start + prefix.length();
        int end = source.indexOf(suffix, valueStart);
        if (end < 0) {
            throw new AuthenticationException("Invalid token");
        }
        return source.substring(valueStart, end);
    }

    private static final class MessageDigestSafeEquals {
        private MessageDigestSafeEquals() {
        }

        static boolean equals(String left, String right) {
            return java.security.MessageDigest.isEqual(
                    left.getBytes(StandardCharsets.UTF_8),
                    right.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}
