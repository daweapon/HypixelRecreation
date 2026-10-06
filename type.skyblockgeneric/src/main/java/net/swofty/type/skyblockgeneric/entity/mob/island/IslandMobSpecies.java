package net.swofty.type.skyblockgeneric.entity.mob.island;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable.LootRecord;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * The monsters that spawn naturally on a Private Island, with the wiki's per-level health, damage,
 * coins and experience orbs for levels 1 to 15.
 */
@Getter
public enum IslandMobSpecies {
    ZOMBIE("Zombie", EntityType.ZOMBIE, Material.ZOMBIE_HEAD, List.of(MobType.UNDEAD), 15,
            new int[]{100, 115, 132, 152, 174, 201, 231, 266, 305, 351, 404, 465, 535, 615, 707},
            new int[]{20, 23, 26, 29, 33, 37, 42, 47, 53, 60, 68, 77, 87, 98, 111},
            new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8},
            new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6},
            () -> List.of(
                    new LootRecord(ItemType.ROTTEN_FLESH, 1, 100),
                    new LootRecord(ItemType.POISONOUS_POTATO, 1, 2),
                    new LootRecord(ItemType.POTATO, 1, 1),
                    new LootRecord(ItemType.CARROT, 1, 1))),
    SKELETON("Skeleton", EntityType.SKELETON, Material.SKELETON_SKULL, List.of(MobType.UNDEAD, MobType.SKELETAL), 15,
            new int[]{100, 115, 132, 152, 174, 201, 231, 266, 305, 351, 404, 465, 535, 615, 707},
            new int[]{15, 17, 19, 22, 24, 28, 31, 35, 40, 45, 51, 58, 65, 73, 83},
            new int[]{1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8},
            new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6},
            () -> List.of(new LootRecord(ItemType.BONE, ThreadLocalRandom.current().nextBoolean() ? 2 : 1, 100))),
    SPIDER("Spider", EntityType.SPIDER, Material.SPIDER_EYE, List.of(MobType.ARTHROPOD), 15,
            new int[]{120, 138, 158, 182, 209, 241, 277, 319, 367, 422, 485, 558, 642, 738, 849},
            new int[]{35, 40, 45, 51, 57, 64, 73, 82, 93, 105, 119, 134, 152, 171, 194},
            new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8},
            new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6},
            () -> List.of(
                    new LootRecord(ItemType.STRING, 1, 100),
                    new LootRecord(ItemType.SPIDER_EYE, 1, 50))),
    CREEPER("Creeper", EntityType.CREEPER, Material.CREEPER_HEAD, List.of(MobType.HUMANOID), 1,
            new int[]{80}, new int[]{20}, new int[]{2}, new int[]{2},
            () -> List.of(new LootRecord(ItemType.GUNPOWDER, 1, 100))),
    ENDERMAN("Enderman", EntityType.ENDERMAN, Material.ENDER_PEARL, List.of(MobType.ENDER), 15,
            new int[]{160, 184, 211, 243, 279, 321, 370, 425, 489, 562, 647, 744, 856, 984, 1132},
            new int[]{40, 45, 51, 58, 65, 74, 83, 94, 106, 120, 136, 153, 173, 196, 221},
            new int[]{2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12},
            new int[]{4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 11, 12, 14},
            () -> List.of(new LootRecord(ItemType.ENDER_PEARL, 1, 100))),
    WITCH("Witch", EntityType.WITCH, Material.POTION, List.of(MobType.HUMANOID), 15,
            new int[]{150, 173, 198, 228, 262, 301, 346, 399, 458, 527, 606, 697, 802, 922, 1061},
            new int[]{20, 23, 26, 29, 33, 37, 42, 47, 53, 60, 68, 77, 87, 98, 111},
            new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
            () -> List.of(
                    new LootRecord(ItemType.GUNPOWDER, 1, 50),
                    new LootRecord(ItemType.GLOWSTONE_DUST, 1, 50),
                    new LootRecord(ItemType.GLASS_BOTTLE, 2, 20)));

    private final String displayName;
    private final EntityType entityType;
    private final Material icon;
    private final List<MobType> mobTypes;
    private final int maxLevel;
    private final int[] health;
    private final int[] damage;
    private final int[] coins;
    private final int[] orbs;
    private final Supplier<List<LootRecord>> drops;

    IslandMobSpecies(String displayName, EntityType entityType, Material icon, List<MobType> mobTypes, int maxLevel,
                     int[] health, int[] damage, int[] coins, int[] orbs, Supplier<List<LootRecord>> drops) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.icon = icon;
        this.mobTypes = mobTypes;
        this.maxLevel = maxLevel;
        this.health = health;
        this.damage = damage;
        this.coins = coins;
        this.orbs = orbs;
        this.drops = drops;
    }

    public int healthAt(int level) {
        return health[clamp(level) - 1];
    }

    public int damageAt(int level) {
        return damage[clamp(level) - 1];
    }

    public int coinsAt(int level) {
        return coins[clamp(level) - 1];
    }

    public int orbsAt(int level) {
        return orbs[clamp(level) - 1];
    }

    public int clamp(int level) {
        return Math.max(1, Math.min(level, maxLevel));
    }

    /**
     * The level island monsters spawn at for an owner with the given Combat level. The wiki lists
     * Combat 0-20 and 32+; levels for Combat 21-31 are not documented, so they rise one level every
     * two Combat levels between the documented Lv9 and Lv15 - an estimate, not a sourced figure.
     */
    public static int levelForCombat(int combatLevel) {
        if (combatLevel >= 32) return 15;
        if (combatLevel > 20) return Math.min(15, 9 + (combatLevel - 19) / 2);
        if (combatLevel == 20) return 9;
        if (combatLevel >= 18) return 8;
        if (combatLevel >= 16) return 7;
        if (combatLevel >= 14) return 6;
        if (combatLevel >= 12) return 5;
        if (combatLevel >= 10) return 4;
        if (combatLevel >= 8) return 3;
        if (combatLevel >= 5) return 2;
        return 1;
    }
}
