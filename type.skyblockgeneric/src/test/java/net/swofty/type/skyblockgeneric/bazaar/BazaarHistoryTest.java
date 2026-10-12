package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBazaarHistory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarHistoryTest {
    @Test
    void agesReadLikeTheRealHistory() {
        assertEquals("36m", BazaarHistory.ago(36 * 60_000L));
        assertEquals("2h", BazaarHistory.ago(2 * 3_600_000L));
        assertEquals("22h", BazaarHistory.ago(22 * 3_600_000L));
        assertEquals("5s", BazaarHistory.ago(5_000L));
        assertEquals("3d", BazaarHistory.ago(3 * 86_400_000L));
    }

    @Test
    void newestEntryComesFirstAndOnlyTheLatestAreKept() {
        List<BazaarHistory.Entry> entries = List.of();
        for (int i = 1; i <= BazaarHistory.CAPACITY + 5; i++) {
            entries = BazaarHistory.withLatest(entries, entry(i));
        }
        assertEquals(BazaarHistory.CAPACITY, entries.size());
        assertEquals(BazaarHistory.CAPACITY + 5, entries.getFirst().amount());
        assertEquals(6, entries.getLast().amount());
    }

    @Test
    void historySurvivesASerializeRoundTrip() {
        List<BazaarHistory.Entry> entries = List.of(entry(3), entry(2), entry(1));
        List<BazaarHistory.Entry> restored = DatapointBazaarHistory.serializer
                .deserialize(DatapointBazaarHistory.serializer.serialize(entries));
        assertEquals(entries.size(), restored.size());
        for (int i = 0; i < entries.size(); i++) {
            assertEquals(entries.get(i).at(), restored.get(i).at());
            assertEquals(entries.get(i).kind(), restored.get(i).kind());
            assertEquals(entries.get(i).item().id(), restored.get(i).item().id());
            assertEquals(entries.get(i).amount(), restored.get(i).amount());
            assertEquals(entries.get(i).coins(), restored.get(i).coins());
            assertEquals(entries.get(i).player().plain(), restored.get(i).player().plain());
        }
    }

    @Test
    void anEmptyHistoryRoundTrips() {
        assertTrue(DatapointBazaarHistory.serializer
                .deserialize(DatapointBazaarHistory.serializer.serialize(List.of())).isEmpty());
    }

    @Test
    void storedHistoryIsCappedWhenLoaded() {
        List<BazaarHistory.Entry> oversized = new ArrayList<>();
        for (int i = 0; i < BazaarHistory.CAPACITY + 10; i++) {
            oversized.add(entry(i));
        }
        assertEquals(BazaarHistory.CAPACITY, DatapointBazaarHistory.serializer
                .deserialize(DatapointBazaarHistory.serializer.serialize(oversized)).size());
    }

    private static BazaarHistory.Entry entry(int amount) {
        return new BazaarHistory.Entry(1_000L + amount, BazaarHistory.Kind.INSTANT_BUY, ItemType.COAL,
                amount, amount * 10.0, Text.literal("player"));
    }
}
