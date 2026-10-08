package net.swofty.type.skyblockgeneric.mythological;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.Rarity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MythologicalCreatureTest {

    @Test
    void eachGriffinRarityUnlocksTwoMoreCreatures() {
        int expected = 2;
        for (Rarity rarity : List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY, Rarity.MYTHIC)) {
            assertEquals(expected, MythologicalCreature.diggableWith(rarity).size(), rarity.name());
            expected += 2;
        }
    }

    @Test
    void spawnChancesMatchTheWiki() {
        assertEquals(50.0, chance(MythologicalCreature.MINOS_HUNTER, Rarity.COMMON, 0), 0.01);
        assertEquals(27.78, chance(MythologicalCreature.MINOS_HUNTER, Rarity.UNCOMMON, 0), 0.01);
        assertEquals(17.39, chance(MythologicalCreature.MINOS_HUNTER, Rarity.LEGENDARY, 0), 0.01);
        assertEquals(1.30, chance(MythologicalCreature.MINOS_INQUISITOR, Rarity.LEGENDARY, 0), 0.01);
        assertEquals(0.26, chance(MythologicalCreature.MANTICORE, Rarity.MYTHIC, 0), 0.01);
    }

    @Test
    void trackingOnlyBoostsElusiveCreatures() {
        assertTrue(MythologicalCreature.SPHINX.isElusive());
        assertFalse(MythologicalCreature.MINOTAUR.isElusive());
        assertEquals(150, MythologicalCreature.SPHINX.spawnWeight(100), 1e-9);
        assertEquals(400, MythologicalCreature.MINOTAUR.spawnWeight(100), 1e-9);
    }

    @Test
    void variantNamesCarryTheGriffinPrefix() {
        MythologicalVariant variant = MythologicalCreature.MINOS_HUNTER.variant(Rarity.MYTHIC).orElseThrow();
        assertEquals("Empyrean Minos Hunter", variant.nameOf("Minos Hunter"));
        assertEquals("MINOS_HUNTER_200", variant.mobId());
    }

    @Test
    void everyPlayerModelCreatureWearsItsHypixelSkin() {
        for (MythologicalCreature creature : MythologicalCreature.values()) {
            boolean playerModel = creature.getEntityType() == EntityType.MANNEQUIN;
            assertEquals(playerModel, MythologicalAppearance.skin(creature) != null, creature.name());
        }
    }

    private static double chance(MythologicalCreature creature, Rarity griffin, double tracking) {
        double total = MythologicalCreature.diggableWith(griffin).stream()
                .mapToDouble(candidate -> candidate.spawnWeight(tracking))
                .sum();
        return creature.spawnWeight(tracking) / total * 100;
    }
}
