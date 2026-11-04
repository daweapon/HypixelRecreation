package dev.daweapon.service.api;

import dev.daweapon.service.api.control.RestoreConfirmations;
import dev.daweapon.service.api.control.Totp;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpTest {
    private static final byte[] SHA1_KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SHA256_KEY = "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] SHA512_KEY =
            "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

    private static String rfc(byte[] key, long time, String algorithm) {
        return Totp.code(key, time / 30, 8, algorithm);
    }

    @Test
    void matchesRfc6238TestVectors() {
        long[] times = {59L, 1111111109L, 1111111111L, 1234567890L, 2000000000L, 20000000000L};
        String[] sha1 = {"94287082", "07081804", "14050471", "89005924", "69279037", "65353130"};
        String[] sha256 = {"46119246", "68084774", "67062674", "91819424", "90698825", "77737706"};
        String[] sha512 = {"90693936", "25091201", "99943326", "93441116", "38618901", "47863826"};
        for (int i = 0; i < times.length; i++) {
            assertEquals(sha1[i], rfc(SHA1_KEY, times[i], "HmacSHA1"), "SHA1 at " + times[i]);
            assertEquals(sha256[i], rfc(SHA256_KEY, times[i], "HmacSHA256"), "SHA256 at " + times[i]);
            assertEquals(sha512[i], rfc(SHA512_KEY, times[i], "HmacSHA512"), "SHA512 at " + times[i]);
        }
    }

    @Test
    void verifiesSixDigitCodesWithOneStepOfDrift() {
        String secret = Totp.base32Encode(SHA1_KEY);
        long now = 1111111111L;
        String current = Totp.codeAt(secret, now);
        assertEquals(6, current.length());
        assertTrue(Totp.verify(secret, current, now));
        assertTrue(Totp.verify(secret, Totp.codeAt(secret, now - 30), now));
        assertTrue(Totp.verify(secret, Totp.codeAt(secret, now + 30), now));
        assertFalse(Totp.verify(secret, Totp.codeAt(secret, now - 90), now));
        assertTrue(Totp.verify(secret, current.substring(0, 3) + " " + current.substring(3), now));
    }

    @Test
    void rejectsMalformedCodes() {
        String secret = Totp.newSecret();
        long now = Instant.now().getEpochSecond();
        assertFalse(Totp.verify(secret, null, now));
        assertFalse(Totp.verify(secret, "", now));
        assertFalse(Totp.verify(secret, "12345", now));
        assertFalse(Totp.verify(secret, "abcdef", now));
        assertFalse(Totp.verify(null, "123456", now));
    }

    @Test
    void base32RoundTripsAndMatchesTheRfc4648Vector() {
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", Totp.base32Encode(SHA1_KEY));
        assertArrayEquals(SHA1_KEY, Totp.base32Decode("gezd gnbv gy3t qojq gezd gnbv gy3t qojq"));
        assertEquals("MZXW6YQ", Totp.base32Encode("foob".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(32, Totp.newSecret().length());
    }

    @Test
    void otpauthUriIsWhatAuthenticatorAppsExpect() {
        String uri = Totp.otpauthUri("JBSWY3DPEHPK3PXP", "daweapon", "Hypixel Control");
        assertEquals("otpauth://totp/Hypixel%20Control:daweapon?secret=JBSWY3DPEHPK3PXP"
                + "&issuer=Hypixel%20Control&algorithm=SHA1&digits=6&period=30", uri);
    }

    @Test
    void restoreConfirmationIsSingleUseAndBoundToTheRequest() {
        RestoreConfirmations confirmations = new RestoreConfirmations();
        Instant now = Instant.parse("2026-11-03T10:00:00Z");
        List<String> parts = List.of("mongo");

        String token = confirmations.issue("device-1", "20261103-040000-scheduled", parts, "live", now);
        assertFalse(confirmations.consume(token, "device-1", "20261103-040000-scheduled", parts, "live",
                now.plus(RestoreConfirmations.TTL).plusSeconds(1)), "expired");

        token = confirmations.issue("device-1", "20261103-040000-scheduled", parts, "scratch", now);
        assertFalse(confirmations.consume(token, "device-1", "20261103-040000-scheduled", parts, "live", now),
                "a scratch confirmation cannot authorise a live restore");
        assertFalse(confirmations.consume(token, "device-1", "20261103-040000-scheduled", parts, "scratch", now),
                "a token is spent by any attempt");

        token = confirmations.issue("device-1", "20261103-040000-scheduled", parts, "live", now);
        assertFalse(confirmations.consume(token, "device-2", "20261103-040000-scheduled", parts, "live", now));

        token = confirmations.issue("device-1", "20261103-040000-scheduled", parts, "live", now);
        assertTrue(confirmations.consume(token, "device-1", "20261103-040000-scheduled", parts, "live", now));
        assertFalse(confirmations.consume(token, "device-1", "20261103-040000-scheduled", parts, "live", now));
    }
}
