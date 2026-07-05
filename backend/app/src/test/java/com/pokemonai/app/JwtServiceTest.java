package com.pokemonai.app;

import com.pokemonai.identity.service.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // 512-bit secret — meets HMAC-SHA256 minimum key length requirement
    private static final String SECRET =
            "super-secret-key-that-is-definitely-long-enough-for-hmac-sha-256-algorithm";

    private final JwtService service = new JwtService(SECRET, 3_600_000L);

    @Test
    void generateTokenAndExtractEmailRoundTrip() {
        String token = service.generateToken("ash@kanto.com");

        assertThat(service.extractEmail(token)).isEqualTo("ash@kanto.com");
    }

    @Test
    void validTokenPassesIsValid() {
        String token = service.generateToken("misty@cerulean.com");

        assertThat(service.isValid(token)).isTrue();
    }

    @Test
    void expiredTokenFailsIsValid() {
        // expirationMs = -1 makes the token expire in the past
        JwtService shortLived = new JwtService(SECRET, -1L);
        String token = shortLived.generateToken("brock@pewter.com");

        assertThat(service.isValid(token)).isFalse();
    }

    @Test
    void tamperedSignatureFailsIsValid() {
        String token = service.generateToken("gary@pallet.com");
        // flip the last character of the signature segment
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        assertThat(service.isValid(tampered)).isFalse();
    }

    @Test
    void blankTokenFailsIsValid() {
        assertThat(service.isValid("")).isFalse();
    }

    @Test
    void nullTokenFailsIsValid() {
        assertThat(service.isValid(null)).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretFailsIsValid() {
        JwtService other = new JwtService(
                "completely-different-secret-key-that-is-also-long-enough-for-hmac", 3_600_000L);
        String tokenFromOther = other.generateToken("team-rocket@giovanni.com");

        assertThat(service.isValid(tokenFromOther)).isFalse();
    }

    @Test
    void twoDistinctUsersProduceDifferentTokens() {
        String t1 = service.generateToken("red@pallet.com");
        String t2 = service.generateToken("blue@pallet.com");

        assertThat(t1).isNotEqualTo(t2);
    }
}
