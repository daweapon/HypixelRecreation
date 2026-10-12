package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
        UUID profile = UUID.randomUUID();
        for (int i = 1; i <= BazaarHistory.CAPACITY + 5; i++) {
            BazaarHistory.record(profile, BazaarHistory.Kind.INSTANT_BUY, ItemType.COAL, i, i * 10.0, Text.literal("player"));
        }
        List<BazaarHistory.Entry> entries = BazaarHistory.entries(profile);
        assertEquals(BazaarHistory.CAPACITY, entries.size());
        assertEquals(BazaarHistory.CAPACITY + 5, entries.getFirst().amount());
        assertEquals(6, entries.getLast().amount());
    }

    @Test
    void unknownProfilesHaveNoHistory() {
        assertTrue(BazaarHistory.entries(UUID.randomUUID()).isEmpty());
    }
}
