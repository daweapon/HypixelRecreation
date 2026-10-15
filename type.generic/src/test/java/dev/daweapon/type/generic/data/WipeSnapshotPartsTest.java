package dev.daweapon.type.generic.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WipeSnapshotPartsTest {
    @Test
    void largeBlobsSplitIntoPartsThatFitInAMongoDocument() {
        String blob = "x".repeat(WipeSnapshotParts.CHUNK_CHARS * 2 + 17);
        List<String> chunks = WipeSnapshotParts.chunk(blob);

        assertEquals(3, chunks.size());
        assertEquals(blob, String.join("", chunks));
        assertTrue(chunks.stream().allMatch(chunk -> chunk.length() * 4L < 16L * 1024 * 1024));
    }

    @Test
    void emptyBlobsStillRoundTrip() {
        assertEquals(List.of(""), WipeSnapshotParts.chunk(""));
    }
}
