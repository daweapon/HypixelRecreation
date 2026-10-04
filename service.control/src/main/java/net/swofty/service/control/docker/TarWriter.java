package net.swofty.service.control.docker;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Builds a minimal single-file USTAR archive.
 *
 * The Docker Engine API's equivalent of `docker cp` takes a tar stream, and a jar deploy only
 * ever needs one file in it, so a tiny writer is preferable to pulling in an archiving library
 * for one call site.
 */
public final class TarWriter {
    private static final int BLOCK = 512;

    private TarWriter() {
    }

    public static byte[] singleFile(String name, byte[] content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(header(name, content.length));
        out.writeBytes(content);

        // Pad the file data to a block boundary, then append the two empty blocks that
        // terminate a tar archive.
        int remainder = content.length % BLOCK;
        if (remainder != 0) {
            out.writeBytes(new byte[BLOCK - remainder]);
        }
        out.writeBytes(new byte[BLOCK * 2]);
        return out.toByteArray();
    }

    private static byte[] header(String name, int size) {
        byte[] header = new byte[BLOCK];

        writeString(header, 0, 100, name);
        writeOctal(header, 100, 8, 0644);        // mode
        writeOctal(header, 108, 8, 0);           // uid
        writeOctal(header, 116, 8, 0);           // gid
        writeOctal(header, 124, 12, size);
        writeOctal(header, 136, 12, System.currentTimeMillis() / 1000);
        header[156] = '0';                       // type flag: regular file
        writeString(header, 257, 6, "ustar");
        header[263] = '0';
        header[264] = '0';

        // The checksum field is computed with itself treated as spaces.
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        long checksum = 0;
        for (byte b : header) {
            checksum += (b & 0xFF);
        }
        writeOctal(header, 148, 7, checksum);
        header[155] = ' ';
        return header;
    }

    private static void writeString(byte[] buffer, int offset, int length, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int count = Math.min(bytes.length, length - 1);
        System.arraycopy(bytes, 0, buffer, offset, count);
    }

    private static void writeOctal(byte[] buffer, int offset, int length, long value) {
        String octal = Long.toOctalString(value);
        // Fixed-width, zero-padded, NUL-terminated.
        while (octal.length() < length - 1) {
            octal = "0" + octal;
        }
        byte[] bytes = octal.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes, 0, buffer, offset, length - 1);
        buffer[offset + length - 1] = 0;
    }
}
