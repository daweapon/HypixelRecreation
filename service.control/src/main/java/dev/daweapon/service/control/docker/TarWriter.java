package dev.daweapon.service.control.docker;

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

    /** Several files under relative paths; Docker creates any missing parent directories. */
    public static byte[] files(java.util.Map<String, byte[]> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (java.util.Map.Entry<String, byte[]> entry : entries.entrySet()) {
            byte[] content = entry.getValue();
            out.writeBytes(header(entry.getKey(), content.length));
            out.writeBytes(content);
            int remainder = content.length % BLOCK;
            if (remainder != 0) {
                out.writeBytes(new byte[BLOCK - remainder]);
            }
        }
        out.writeBytes(new byte[BLOCK * 2]);
        return out.toByteArray();
    }

    /**
     * Splits a path into the USTAR prefix and name fields (154 and 99 usable bytes), breaking
     * at a slash. Configuration paths routinely exceed the 99-byte name field on its own.
     */
    public static String[] splitPath(String path) {
        if (path.getBytes(StandardCharsets.UTF_8).length <= 99) {
            return new String[]{"", path};
        }
        for (int slash = path.indexOf('/'); slash >= 0; slash = path.indexOf('/', slash + 1)) {
            String prefix = path.substring(0, slash);
            String name = path.substring(slash + 1);
            if (prefix.getBytes(StandardCharsets.UTF_8).length <= 154
                    && name.getBytes(StandardCharsets.UTF_8).length <= 99) {
                return new String[]{prefix, name};
            }
        }
        throw new IllegalArgumentException("Path too long for a USTAR header: " + path);
    }

    private static byte[] header(String path, int size) {
        byte[] header = new byte[BLOCK];
        String[] split = splitPath(path);
        String name = split[1];
        writeString(header, 345, 155, split[0]);

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
