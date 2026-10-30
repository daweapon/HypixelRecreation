package dev.daweapon.type.skyblockgeneric.healing;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentLastStand;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.WitherShieldEffect;
import dev.daweapon.type.skyblockgeneric.hunting.GhastlyDistraction;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VitalityRegenTest {
    @Test
    void drainRegensHalfAVitalityPerLevel() {
        assertEquals(0.5, VitalityRegen.drainAmount(1));
        assertEquals(2.5, VitalityRegen.drainAmount(5));
        assertEquals(3, VitalityRegen.drainAmount(6));
    }

    @Test
    void cooldownBlocksUntilItElapses() {
        UUID id = UUID.randomUUID();
        Map<UUID, Long> last = Map.of(id, 1000L);
        assertFalse(VitalityRegen.offCooldown(last, id, 1000, 1999));
        assertTrue(VitalityRegen.offCooldown(last, id, 1000, 2000));
        assertTrue(VitalityRegen.offCooldown(Map.of(), id, 1000, 0));
    }

    @Test
    void regenIsClampedToTheMaximum() {
        assertEquals(100f, VitalityRegen.add(98, 5, 100));
        assertEquals(55f, VitalityRegen.add(50, 5, 100));
    }

    @Test
    void lastStandScalesPerLevelAndTriggersBelowFortyPercent() {
        assertEquals(12.5, EnchantmentLastStand.defensePercent(5));
        assertEquals(10, EnchantmentLastStand.vitality(5));
        assertTrue(EnchantmentLastStand.crossesThreshold(500, 150, 1000));
        assertFalse(EnchantmentLastStand.crossesThreshold(500, 50, 1000));
    }

    @Test
    void witherShieldAbsorptionAndRefundFollowTheWiki() {
        assertEquals(984, WitherShieldEffect.shieldAmount(24));
        assertEquals(1400, WitherShieldEffect.shieldAmount(50));
        assertEquals(50, WitherShieldEffect.refund(984, 984));
        assertEquals(25, WitherShieldEffect.refund(1000, 500));
        assertEquals(0, WitherShieldEffect.refund(1000, 0));
    }

    @Test
    void ghastlyDistractionPacifiesUntilExpiryAndSkipsExemptMobs() {
        assertTrue(GhastlyDistraction.pacifies(2000, 1000, false));
        assertFalse(GhastlyDistraction.pacifies(1000, 1000, false));
        assertFalse(GhastlyDistraction.pacifies(2000, 1000, true));
        assertTrue(GhastlyDistraction.isOpalGemstone(Material.WHITE_STAINED_GLASS, RegionType.SMOLDERING_TOMB));
        assertFalse(GhastlyDistraction.isOpalGemstone(Material.WHITE_STAINED_GLASS, RegionType.THE_END));
    }
}
