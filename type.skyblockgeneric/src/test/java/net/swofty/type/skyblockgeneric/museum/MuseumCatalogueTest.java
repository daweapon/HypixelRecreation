package net.swofty.type.skyblockgeneric.museum;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MuseumCatalogueTest {
    private static final MuseumCatalogue CATALOGUE =
            MuseumCatalogue.fromFile(new File("../configuration/skyblock/museum/catalogue.yml"));

    @Test
    void catalogueMatchesHypixelsMuseum() {
        Map<MuseumCategory, Long> sizes = CATALOGUE.all().stream()
                .collect(Collectors.groupingBy(MuseumEntry::category, Collectors.counting()));
        assertEquals(245, sizes.get(MuseumCategory.COMBAT));
        assertEquals(101, sizes.get(MuseumCategory.FARMING));
        assertEquals(74, sizes.get(MuseumCategory.MINING));
        assertEquals(66, sizes.get(MuseumCategory.FISHING));
        assertEquals(27, sizes.get(MuseumCategory.FORAGING));
        assertEquals(98, sizes.get(MuseumCategory.DUNGEONEERING));
        assertEquals(25, sizes.get(MuseumCategory.HUNTING));
        assertEquals(337, sizes.get(MuseumCategory.SPECIAL));
        assertEquals(3646, CATALOGUE.totalXp());
    }

    @Test
    void upgradeChainsPointAtRealEntries() {
        for (MuseumEntry entry : CATALOGUE.all()) {
            if (entry.parent() != null) {
                assertTrue(CATALOGUE.entry(entry.parent()).isPresent(), entry.id() + " -> " + entry.parent());
            }
        }
        MuseumEntry aspectOfTheEnd = CATALOGUE.entry("ASPECT_OF_THE_END").orElseThrow();
        assertEquals("ASPECT_OF_THE_VOID", aspectOfTheEnd.parent());
    }

    @Test
    void armorSetsAreDonatedFromAnyPiece() {
        MuseumEntry lapis = CATALOGUE.entry("LAPIS_ARMOR").orElseThrow();
        assertTrue(lapis.isSet());
        assertTrue(lapis.isAvailable());
        assertEquals(lapis, CATALOGUE.entryFor(ItemType.LAPIS_ARMOR_BOOTS).orElseThrow());
    }

    @Test
    void milestonesFollowEveryHundredXp() {
        assertEquals(0, MuseumMilestones.milestoneFor(99));
        assertEquals(3, MuseumMilestones.milestoneFor(300));
        assertEquals(36, MuseumMilestones.milestoneFor(3646));
        assertEquals(1.72D, MuseumMilestones.bankInterestMultiplier(36), 1e-9);
    }
}
