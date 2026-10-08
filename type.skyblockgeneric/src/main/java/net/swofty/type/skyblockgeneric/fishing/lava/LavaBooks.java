package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves an Enchanted Book drop's enchantment by name, trying each {@code |}-separated alias, so a book whose
 * enchantment this server doesn't have yet stays catalogued and never drops blank.
 */
final class LavaBooks {
    private LavaBooks() {
    }

    static @Nullable EnchantmentType resolve(String names) {
        for (String name : names.split("\\|")) {
            try {
                return EnchantmentType.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
