package dev.daweapon.type.skyblockgeneric.runecrafting;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class RunicMobs {
    public static final double SPAWN_CHANCE_PERCENT = 0.5;
    public static final float HEALTH_MULTIPLIER = 4;
    public static final double DAMAGE_MULTIPLIER = 2;
    private static final double MINIMUM_KILL_XP = 10;
    private static final double KILL_XP_FACTOR = 2;

    private static final List<ItemType> RUNES = List.of(
            ItemType.SNOW_RUNE, ItemType.LAVA_RUNE, ItemType.GEM_RUNE, ItemType.ZAP_RUNE,
            ItemType.WHITE_SPIRAL_RUNE, ItemType.HOT_RUNE, ItemType.HEARTS_RUNE, ItemType.SPARKLING_RUNE,
            ItemType.GOLDEN_RUNE, ItemType.MUSIC_RUNE, ItemType.CLOUDS_RUNE, ItemType.LIGHTNING_RUNE,
            ItemType.RAINBOW_RUNE);

    private RunicMobs() {
    }

    public static boolean rollSpawn(RandomGenerator random) {
        return random.nextDouble() * 100 < SPAWN_CHANCE_PERCENT;
    }

    public static void rollSpawn(SkyBlockMob mob) {
        if (rollSpawn(ThreadLocalRandom.current())) mob.makeRunic();
    }

    public static double damageMultiplier(SkyBlockMob mob) {
        return mob.isRunic() ? DAMAGE_MULTIPLIER : 1;
    }

    public static double weight(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 8;
            case UNCOMMON -> 5;
            case RARE -> 3;
            case EPIC -> 2;
            default -> 1;
        };
    }

    public static ItemType pickRune(UUID player, RandomGenerator random) {
        return LootRoll.pick(player, RUNES, rune -> rune, rune -> weight(rune.rarity), random);
    }

    public static List<ItemType> runes() {
        return RUNES;
    }

    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        double xp = Math.max(MINIMUM_KILL_XP, mob.getOtherLoot().getSkillXPAmount() * KILL_XP_FACTOR);
        player.getSkills().increase(player, SkillCategories.RUNECRAFTING, xp);
        mob.awardRune(player, pickRune(player.getUuid(), ThreadLocalRandom.current()));
    }
}
