package dev.daweapon.type.skyblockgeneric.enchantment;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFlowstate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantmentFlowstateTest {
    @Test
    void streakGrowsPerBlockCapsAndExpires() {
        for (int i = 0; i < 250; i++) EnchantmentFlowstate.onBlockMined(null);
        long now = System.currentTimeMillis();
        assertEquals(EnchantmentFlowstate.MAX_BLOCKS, EnchantmentFlowstate.blocks(null, now));
        assertEquals(0, EnchantmentFlowstate.blocks(null, now + EnchantmentFlowstate.RESET_MILLIS + 1));
    }
}
