package dev.daweapon.service.control.docker;

import java.nio.charset.StandardCharsets;

/**
 * Un-frames Docker's multiplexed log stream.
 *
 * When a container runs without a TTY, Docker prefixes every chunk with an 8-byte header:
 * one byte of stream type (1 = stdout, 2 = stderr), three zero bytes, then a big-endian
 * 4-byte payload length. Containers started with a TTY emit plain bytes instead, so the
 * decoder falls back to returning the input unchanged when the framing does not hold.
 */
public final class LogFrameDecoder {
    private static final int HEADER_LENGTH = 8;
    private static final int STREAM_STDIN = 0;
    private static final int STREAM_STDERR = 2;

    private LogFrameDecoder() {
    }

    public static String decode(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return "";
        }
        if (!looksFramed(raw)) {
            return new String(raw, StandardCharsets.UTF_8);
        }

        StringBuilder out = new StringBuilder(raw.length);
        int offset = 0;
        while (offset + HEADER_LENGTH <= raw.length) {
            int length = readLength(raw, offset);
            offset += HEADER_LENGTH;
            if (length < 0 || offset + length > raw.length) {
                // Truncated final frame - emit whatever remains rather than dropping it.
                out.append(new String(raw, offset, raw.length - offset, StandardCharsets.UTF_8));
                break;
            }
            out.append(new String(raw, offset, length, StandardCharsets.UTF_8));
            offset += length;
        }
        return out.toString();
    }

    /**
     * Validates the framing by walking the whole stream. A single plausible-looking header is
     * not enough: ordinary log text can coincidentally start with bytes in the valid range.
     */
    private static boolean looksFramed(byte[] raw) {
        int offset = 0;
        int frames = 0;
        while (offset + HEADER_LENGTH <= raw.length) {
            int streamType = raw[offset] & 0xFF;
            if (streamType < STREAM_STDIN || streamType > STREAM_STDERR) {
                return false;
            }
            if (raw[offset + 1] != 0 || raw[offset + 2] != 0 || raw[offset + 3] != 0) {
                return false;
            }
            int length = readLength(raw, offset);
            if (length < 0) {
                return false;
            }
            offset += HEADER_LENGTH + length;
            frames++;
        }
        return frames > 0 && offset == raw.length;
    }

    private static int readLength(byte[] raw, int offset) {
        return ((raw[offset + 4] & 0xFF) << 24)
                | ((raw[offset + 5] & 0xFF) << 16)
                | ((raw[offset + 6] & 0xFF) << 8)
                | (raw[offset + 7] & 0xFF);
    }
}
