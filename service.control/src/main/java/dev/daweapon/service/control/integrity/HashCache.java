package dev.daweapon.service.control.integrity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SHA-256 of host files, recomputed only when a file's size or modification time changes.
 *
 * The NAS configuration tree holds thousands of files including world data; rehashing all of it
 * on every check would make the drift screen slow enough that nobody opens it.
 */
public final class HashCache {
    private record Cached(long size, long modifiedMillis, String sha256) {
    }

    private final Map<Path, Cached> cache = new ConcurrentHashMap<>();

    public String sha256(Path file) throws IOException {
        long size = Files.size(file);
        long modified = Files.getLastModifiedTime(file).toMillis();
        Cached cached = cache.get(file);
        if (cached != null && cached.size() == size && cached.modifiedMillis() == modified) {
            return cached.sha256();
        }
        String hash = digest(file, "SHA-256");
        cache.put(file, new Cached(size, modified, hash));
        return hash;
    }

    public static String digest(Path file, String algorithm) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return digest(in, algorithm);
        }
    }

    public static String digest(InputStream in, String algorithm) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " unavailable", e);
        }
        byte[] buffer = new byte[64 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public void retainOnly(java.util.Set<Path> present) {
        cache.keySet().retainAll(present);
    }
}
