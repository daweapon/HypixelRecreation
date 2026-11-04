package dev.daweapon.service.control.docker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracts the single file Docker's archive endpoint wraps in a tar.
 *
 * Copying one file out of a container returns it inside a tar stream, sometimes preceded by
 * PAX extended headers; those are skipped and the first regular file is streamed out.
 */
public final class TarReader {
    private static final int BLOCK = TarWriter.BLOCK;

    private TarReader() {
    }

    public static long extractFirstFile(Path tar, Path destination) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(tar), 65536);
             OutputStream out = Files.newOutputStream(destination)) {
            return extractFirstFile(in, out);
        }
    }

    /** Streams the first regular file to {@code out} and returns its size. */
    public static long extractFirstFile(InputStream in, OutputStream out) throws IOException {
        byte[] header = new byte[BLOCK];
        while (true) {
            if (in.readNBytes(header, 0, BLOCK) < BLOCK || isZero(header)) {
                throw new IOException("Archive contains no regular file");
            }
            long size = parseSize(header);
            byte type = header[156];
            if (type == '0' || type == 0) {
                copy(in, out, size);
                return size;
            }
            skip(in, size + TarWriter.padding(size));
        }
    }

    private static long parseSize(byte[] header) {
        // GNU base-256 encoding, used by some writers for files over 8 GiB.
        if ((header[124] & 0x80) != 0) {
            long value = header[124] & 0x7F;
            for (int i = 125; i < 136; i++) {
                value = (value << 8) | (header[i] & 0xFF);
            }
            return value;
        }
        long value = 0;
        for (int i = 124; i < 136; i++) {
            byte b = header[i];
            if (b == 0 || b == ' ') {
                if (value > 0) {
                    break;
                }
                continue;
            }
            value = value * 8 + (b - '0');
        }
        return value;
    }

    private static void copy(InputStream in, OutputStream out, long size) throws IOException {
        byte[] buffer = new byte[65536];
        long remaining = size;
        while (remaining > 0) {
            int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read < 0) {
                throw new IOException("Archive ended " + remaining + " bytes early");
            }
            out.write(buffer, 0, read);
            remaining -= read;
        }
    }

    private static void skip(InputStream in, long count) throws IOException {
        long remaining = count;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped <= 0) {
                if (in.read() < 0) {
                    throw new IOException("Archive ended inside an entry");
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    private static boolean isZero(byte[] block) {
        for (byte b : block) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
