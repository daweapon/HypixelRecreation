package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.skyblock.item.PotatoType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.StandardItemComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Wiki Dungeon Reward Chest "Item Upgrades": weapons and armor bought from a chest may come recombobulated,
 * with Growth VI or Protection VI, or with 1-5 stars (Master Mode doubles the star chances).
 */
public final class DungeonChestUpgrades {
    static final double RARITY_UPGRADE_CHANCE = 1;
    static final double GROWTH_CHANCE = 1;
    static final double PROTECTION_CHANCE = 1;
    static final double[] STAR_CHANCES = {10, 5, 3, 2, 1};

    private DungeonChestUpgrades() {
    }

    public static void apply(UUID player, SkyBlockItem item, boolean masterMode) {
        apply(player, item, masterMode, ThreadLocalRandom.current());
    }

    public static void apply(@Nullable UUID player, SkyBlockItem item, boolean masterMode, RandomGenerator random) {
        PotatoType kind = upgradeable(item);
        if (kind == null) return;
        if (LootRoll.roll(player, null, RARITY_UPGRADE_CHANCE, random)) item.getAttributeHandler().setRecombobulated(true);
        if (kind == PotatoType.ARMOR) {
            if (LootRoll.roll(player, null, GROWTH_CHANCE, random)) {
                item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.GROWTH, 6));
            }
            if (LootRoll.roll(player, null, PROTECTION_CHANCE, random)) {
                item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.PROTECTION, 6));
            }
        }
        int stars = stars(player, masterMode, random);
        if (stars > 0) item.getAttributeHandler().setUpgradeLevel(stars);
    }

    static int stars(@Nullable UUID player, boolean masterMode, RandomGenerator random) {
        double factor = masterMode ? 2 : 1;
        List<Integer> outcomes = List.of(1, 2, 3, 4, 5, 0);
        double none = 100 - Arrays.stream(STAR_CHANCES).sum() * factor;
        return LootRoll.pick(player, outcomes, _ -> null,
                stars -> stars == 0 ? none : STAR_CHANCES[stars - 1] * factor, random);
    }

    private static @Nullable PotatoType upgradeable(SkyBlockItem item) {
        if (!item.hasComponent(StandardItemComponent.class)) return null;
        return item.getComponent(StandardItemComponent.class).getType().getPotatoType();
    }
}
