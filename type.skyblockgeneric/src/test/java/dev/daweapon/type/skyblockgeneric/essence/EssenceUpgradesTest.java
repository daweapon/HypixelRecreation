package dev.daweapon.type.skyblockgeneric.essence;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssenceUpgradesTest {
    private static final Path DIRECTORY = Path.of("../configuration/skyblock");

    @Test
    void importsEveryNeuUpgrade() {
        Map<String, EssenceUpgrades.Upgrade> upgrades = EssenceUpgrades.loadUpgrades(DIRECTORY.resolve("essence_upgrades.json"));
        assertEquals(529, upgrades.size());
        EssenceUpgrades.Upgrade ragnarock = upgrades.get("RAGNAROCK_AXE");
        assertEquals(EssenceType.CRIMSON, ragnarock.essence());
        assertEquals(10, ragnarock.maxStars());
        assertEquals(100, ragnarock.star(1).essence());
        assertEquals(1_000_000, ragnarock.star(10).coins());
        assertEquals(5, ragnarock.star(10).items().get("HEAVY_PEARL"));
    }

    @Test
    void salvageTableMatchesTheWiki() {
        Map<String, EssenceUpgrades.Salvage> salvage = EssenceUpgrades.loadSalvage(DIRECTORY.resolve("essence_salvage.json"));
        assertEquals(21, salvage.size());
        assertEquals(5, salvage.get("RAMPART_HELMET").amount());
        assertEquals(3, salvage.get("FLAMING_FIST").items().get("LEATHER_CLOTH"));
    }

    @Test
    void starsRecolourPastFive() {
        assertEquals("<6>✪✪✪", EssenceStars.display(3));
        assertEquals("<d>✪✪<6>✪✪✪", EssenceStars.display(7));
        assertEquals("<b>✪<d>✪✪✪✪", EssenceStars.display(11));
        assertTrue(EssenceStars.display(0).isEmpty());
    }
}
