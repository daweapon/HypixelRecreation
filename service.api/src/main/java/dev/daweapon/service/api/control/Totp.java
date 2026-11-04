package dev.daweapon.service.api.control;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Time-based one-time passwords (RFC 6238 over RFC 4226), as used by every authenticator app:
 * HMAC-SHA1, 6 digits, 30-second steps, one step of clock drift tolerated either side.
 */
public final class Totp {
    public static final int DIGITS = 6;
    public static final long PERIOD_SECONDS = 30;
    public static final int DRIFT_STEPS = 1;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Totp() {
    }

    public static String newSecret() {
        byte[] key = new byte[20];
        RANDOM.nextBytes(key);
        return base32Encode(key);
    }

    public static String code(byte[] key, long counter, int digits, String hmacAlgorithm) {
        try {
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(new SecretKeySpec(key, hmacAlgorithm));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(hmacAlgorithm + " unavailable", e);
        }
    }

    public static String codeAt(String base32Secret, long epochSeconds) {
        return code(base32Decode(base32Secret), Math.floorDiv(epochSeconds, PERIOD_SECONDS), DIGITS, "HmacSHA1");
    }

    /** True when {@code code} matches the current step or one step either side. */
    public static boolean verify(String base32Secret, String code, long epochSeconds) {
        if (base32Secret == null || code == null) {
            return false;
        }
        String candidate = code.replaceAll("\\s", "");
        if (!candidate.matches("\\d{" + DIGITS + "}")) {
            return false;
        }
        byte[] key = base32Decode(base32Secret);
        long step = Math.floorDiv(epochSeconds, PERIOD_SECONDS);
        boolean matched = false;
        for (int drift = -DRIFT_STEPS; drift <= DRIFT_STEPS; drift++) {
            String expected = code(key, step + drift, DIGITS, "HmacSHA1");
            matched |= MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    candidate.getBytes(StandardCharsets.US_ASCII));
        }
        return matched;
    }

    public static String otpauthUri(String base32Secret, String account, String issuer) {
        String label = encode(issuer) + ":" + encode(account);
        return "otpauth://totp/" + label + "?secret=" + base32Secret + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD_SECONDS;
    }

    public static String base32Encode(byte[] data) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                out.append(ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(ALPHABET.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return out.toString();
    }

    public static byte[] base32Decode(String value) {
        String clean = value.replace("=", "").replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buffer = 0;
        int bits = 0;
        for (char c : clean.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index < 0) {
                throw new IllegalArgumentException("Not a base32 secret");
            }
            buffer = (buffer << 5) | index;
            bits += 5;
            if (bits >= 8) {
                out.write((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return out.toByteArray();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
