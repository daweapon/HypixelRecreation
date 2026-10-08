package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Accessory effects on coins, bits, potions, bait and thunder charge. Each number is the effect's
 * {@code percent} in the accessory YAML (coins for the per-minute and per-level handlers).
 */
public final class AccessoryEconomyHooks {
    public static final String COINS_PER_MINUTE = "COINS_PER_MINUTE";
    public static final String COINS_PER_MOB_LEVEL = "COINS_PER_MOB_LEVEL";
    public static final String DOUBLE_BITS = "DOUBLE_BITS_CHANCE";
    public static final String POTION_DURATION = "POTION_DURATION";
    public static final String BAIT_PRESERVATION = "BAIT_PRESERVATION";
    public static final String THUNDER_CHARGE = "THUNDER_CHARGE";

    private AccessoryEconomyHooks() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.getLoadedPlayers().forEach(AccessoryEconomyHooks::payMinute))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    static void payMinute(SkyBlockPlayer player) {
        double coins = AccessoryHandlerEffects.percent(player, COINS_PER_MINUTE);
        if (coins > 0) player.addCoins(coins);
    }

    public static int mobLevelCoins(SkyBlockPlayer player, SkyBlockMob mob) {
        double perLevel = AccessoryHandlerEffects.percent(player, COINS_PER_MOB_LEVEL);
        if (perLevel <= 0 || mob == null || mob.getLevel() == null) return 0;
        return (int) Math.round(perLevel * Math.max(0, mob.getLevel()));
    }

    public static int bitsGained(SkyBlockPlayer player, int bits) {
        double chance = AccessoryHandlerEffects.percent(player, DOUBLE_BITS);
        if (chance <= 0 || bits <= 0) return bits;
        return LootRoll.roll(player.getUuid(), null, chance) ? bits * 2 : bits;
    }

    public static double potionDurationMultiplier(SkyBlockPlayer player) {
        return 1 + AccessoryHandlerEffects.percent(player, POTION_DURATION) / 100D;
    }

    public static boolean preservesBait(SkyBlockPlayer player) {
        double chance = AccessoryHandlerEffects.percent(player, BAIT_PRESERVATION);
        return chance > 0 && LootRoll.roll(player.getUuid(), null, chance);
    }

    public static double thunderChargeBonus(SkyBlockPlayer player) {
        return AccessoryHandlerEffects.percent(player, THUNDER_CHARGE) / 100D;
    }
}
