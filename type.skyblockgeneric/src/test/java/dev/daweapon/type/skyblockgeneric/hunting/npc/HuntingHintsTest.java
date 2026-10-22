package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntingHintsTest {
    private final List<HuntingHints.Hint> hints = HuntingHints.load(new File("../configuration/skyblock/hunting/freddy_hints.yml"));

    @Test
    void freddyOffersTwentyFourHintsWithUniqueIds() {
        assertEquals(24, hints.size());
        assertEquals(24, new HashSet<>(hints.stream().map(HuntingHints.Hint::id).toList()).size());
    }

    @Test
    void everyHintHasLinesAndARealShardIcon() {
        for (HuntingHints.Hint hint : hints) {
            assertFalse(hint.lines().isEmpty(), hint.id());
            assertTrue(AttributeRegistry.findByShard(hint.shard()).isPresent(), hint.shard());
        }
    }

    @Test
    void pricesFollowTheWikiTiers() {
        assertEquals(5_000, HuntingHints.byId(hints, "ant").coins());
        assertEquals(20_000, HuntingHints.byId(hints, "beeheemoth").coins());
        assertEquals(20_000, HuntingHints.byId(hints, "mountain_goat").coins());
        assertEquals(15_000, HuntingHints.byId(hints, "pangolin").coins());
        assertEquals(10_000, HuntingHints.byId(hints, "tiki_trio").coins());
        assertEquals(5_000, HuntingHints.byId(hints, "timil").coins());
    }

    @Test
    void irregularPluralsAreKept() {
        assertEquals("Firefoxes", HuntingHints.byId(hints, "firefox").plural());
        assertEquals("Honeybuzz", HuntingHints.byId(hints, "honeybuzz").plural());
        assertEquals("Ants", HuntingHints.byId(hints, "ant").plural());
    }

    @Test
    void dialogueKeysAreDistinctFromTheFirstInteraction() {
        assertEquals("hint-ant", HuntingHints.dialogueKey(HuntingHints.byId(hints, "ant")));
    }
}
