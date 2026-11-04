package dev.daweapon.service.control.docker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Builds minimal USTAR archives.
 *
 * The Docker Engine API's equivalent of `docker cp` takes a tar stream. A jar deploy needs one
 * file held in memory; a database restore needs a few files streamed from disk, since a dump
 * can be far larger than is sensible to buffer. A tiny writer is preferable to pulling in an
 * archiving library for these call sites.
 */
public final class TarWriter {
    static final int BLOCK = 512;

    /** One file in an archive: either in memory or streamed from disk. */
    public record Entry(String name, Path source, byte[] content) {
        public static Entry file(String name, Path source) {
            return new Entry(name, source, null);
        }

        public static Entry bytes(String name, byte[] content) {
            return new Entry(name, null, content);
        }

        long size() throws IOException {
            return content != null ? content.length : Files.size(source);
        }
    }

    private TarWriter() {
    }

    public static byte[] singleFile(String name, byte[] content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write(out, List.of(Entry.bytes(name, content)));
        } catch (IOException e) {
            throw new IllegalStateException("In-memory tar write failed", e);
        }
        return out.toByteArray();
    }

    /** Exact byte length of the archive {@link #write} produces, needed for Content-Length. */
    public static long length(List<Entry> entries) throws IOException {
        long total = BLOCK * 2L;
        for (Entry entry : entries) {
            total += BLOCK + entry.size() + padding(entry.size());
        }
        return total;
    }

    public static void write(OutputStream out, List<Entry> entries) throws IOException {
        for (Entry entry : entries) {
            long size = entry.size();
            out.write(header(entry.name(), size));
            if (entry.content() != null) {
                out.write(entry.content());
            } else {
                try (InputStream in = Files.newInputStream(entry.source())) {
                    long copied = in.transferTo(out);
                    if (copied != size) {
                        throw new IOException(entry.source() + " changed size while being archived");
                    }
                }
            }
            // Pad the file data to a block boundary.
            out.write(new byte[(int) padding(size)]);
        }
        // Two empty blocks terminate a tar archive.
        out.write(new byte[BLOCK * 2]);
    }

    static long padding(long size) {
        long remainder = size % BLOCK;
        return remainder == 0 ? 0 : BLOCK - remainder;
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

    private static byte[] header(String path, long size) {
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
