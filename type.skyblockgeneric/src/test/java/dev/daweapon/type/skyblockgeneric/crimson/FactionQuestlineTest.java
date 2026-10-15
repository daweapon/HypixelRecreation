package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.type.skyblockgeneric.crimson.questline.BarbarianStep;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestMobs;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.crimson.questline.UniversalStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;

class FactionQuestlineTest {

    @Test
    void mobIdsMatchWhateverSpellingTheMobUses() {
        assertEquals("BLAZE", CrimsonQuestMobs.normalise("BLAZE_70"));
        assertEquals("BLAZE", CrimsonQuestMobs.normalise("<6>Blaze"));
        assertEquals("CORRUPTED_FLAMING_SPIDER", CrimsonQuestMobs.normalise("<5>Corrupted Flaming Spider"));
        assertEquals("MILLENNIA_AGED_BLAZE", CrimsonQuestMobs.normalise("Millennia-Aged Blaze"));
        assertTrue(CrimsonQuestMobs.matches("INTRO_BLAZE", CrimsonQuestMobs.BLAZES));
        assertTrue(CrimsonQuestMobs.matches("MAGE_OUTLAW", CrimsonQuestMobs.MAGE_OUTLAW));
        assertTrue(CrimsonQuestMobs.matches("MAGMA_BOSS", CrimsonQuestMobs.MAGMA_BOSS));
    }

    @Test
    void savedStagesKeepTheirOrdinals() {
        assertEquals(List.of("KILL_BLAZES", "HELP_ELLE", "BRING_ITEMS", "EXPLORE", "TALK_TO_VILLAGERS", "CHOOSE_FACTION", "COMPLETE"),
                Arrays.stream(UniversalStep.values()).map(Enum::name).toList());
        assertEquals(22, MageStep.DEFEAT_KUUDRA.ordinal());
        assertEquals(24, MageStep.COMPLETE.ordinal());
        assertEquals(22, BarbarianStep.DEFEAT_KUUDRA.ordinal());
        assertEquals(24, BarbarianStep.COMPLETE.ordinal());
    }
}
