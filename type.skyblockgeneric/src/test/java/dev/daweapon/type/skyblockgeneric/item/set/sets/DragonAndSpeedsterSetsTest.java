package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DragonAndSpeedsterSetsTest {
    @Test
    void speedsterAddsTwentySpeed() {
        assertEquals(20, new SpeedsterSet().getStatistics().getOverall(ItemStatistic.SPEED), 1e-9);
    }

    @Test
    void youngBloodNeedsMoreThanHalfHealth() {
        assertEquals(70, YoungDragonSet.speedBonus(51, 100), 1e-9);
        assertEquals(0, YoungDragonSet.speedBonus(50, 100), 1e-9);
        assertEquals(0, YoungDragonSet.speedBonus(10, 100), 1e-9);
        assertEquals(0, YoungDragonSet.speedBonus(10, 0), 1e-9);
    }

    @Test
    void protectiveBloodScalesWithMissingHealth() {
        assertEquals(25, ProtectorDragonSet.missingHealthPercent(75, 100), 1e-9);
        assertEquals(0, ProtectorDragonSet.missingHealthPercent(120, 100), 1e-9);
        assertEquals(100, ProtectorDragonSet.missingHealthPercent(-5, 100), 1e-9);
        assertEquals(100, ProtectorDragonSet.bonusDefense(400, 25), 1e-9);
    }

    @Test
    void superiorBloodBoostsEveryCombatStatByFivePercent() {
        ItemStatistics stats = new SuperiorDragonSet().getStatistics();
        for (ItemStatistic stat : SuperiorDragonSet.BOOSTED) {
            assertEquals(1.05, stats.getStatisticsMultiplicative().get(stat), 1e-9, stat.name());
        }
        assertTrue(SuperiorDragonSet.BOOSTED.contains(ItemStatistic.MAGIC_FIND));
    }

    @Test
    void setsAreRegisteredWithAllFourPieces() {
        assertEquals(ArmorSetRegistry.YOUNG_DRAGON, ArmorSetRegistry.getArmorSet(ItemType.YOUNG_DRAGON_HELMET));
        assertEquals(ArmorSetRegistry.PROTECTOR_DRAGON, ArmorSetRegistry.getArmorSet(ItemType.PROTECTOR_DRAGON_BOOTS));
        assertEquals(ArmorSetRegistry.SUPERIOR_DRAGON, ArmorSetRegistry.getArmorSet(ItemType.SUPERIOR_DRAGON_LEGGINGS));
        assertEquals(ArmorSetRegistry.SPEEDSTER, ArmorSetRegistry.getArmorSet(ItemType.SPEEDSTER_CHESTPLATE));
        assertEquals(4, ArmorSetRegistry.getPieceCount(ArmorSetRegistry.SPEEDSTER));
    }
}
