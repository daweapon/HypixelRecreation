package dev.daweapon.type.skyblockgeneric.gui.inventories.enchantment;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentGuideTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void everyEnchantmentIsInAtLeastOneFilterAndAllShowsEveryone() {
        for (EnchantmentType type : EnchantmentType.values()) {
            assertTrue(EnchantmentGuideFilter.ALL.matches(type));
            boolean inACategory = Arrays.stream(EnchantmentGuideFilter.values())
                    .filter(filter -> filter != EnchantmentGuideFilter.ALL)
                    .anyMatch(filter -> filter.matches(type));
            if (!inACategory) assertTrue(type.getEnch().getGroups().isEmpty() || type.getEnch().getGroups().stream()
                    .allMatch(group -> group.name().equals("TOOLS") || group.name().equals("SHOVEL")), type.name() + " is in no category");
        }
    }

    @Test
    void guideTextExistsForEveryEnchantment() {
        for (EnchantmentType type : EnchantmentType.values()) {
            assertFalse(EnchantmentGuideLore.appliedTo(type).isEmpty(), type.name());
            assertTrue(EnchantmentGuideLore.title(type, 1).contains(type.getName()), type.name());
        }
    }

    @Test
    void filtersCycleBothWaysAndWrap() {
        EnchantmentGuideFilter first = EnchantmentGuideFilter.values()[0];
        assertEquals(first, first.next(1).next(-1));
        assertEquals(first, first.next(-1).next(1));
        assertEquals(first, first.next(EnchantmentGuideFilter.values().length));
        assertTrue(EnchantmentGuideFilter.ULTIMATE.matches(EnchantmentType.COMBO));
        assertFalse(EnchantmentGuideFilter.ULTIMATE.matches(EnchantmentType.SHARPNESS));
        assertTrue(EnchantmentGuideFilter.BOW.matches(EnchantmentType.POWER));
    }
}
