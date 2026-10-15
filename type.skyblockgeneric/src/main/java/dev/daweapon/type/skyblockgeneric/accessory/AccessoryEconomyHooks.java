package dev.daweapon.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Accessory effects on coins, bits, potions, bait, thunder charge, minions and chocolate. Each number
 * is the effect's {@code percent} in the accessory YAML; for the coin and chocolate handlers it is an
 * amount, not a percentage.
 */
public final class AccessoryEconomyHooks {
    public static final String COINS_PER_MINUTE = "COINS_PER_MINUTE";
    public static final String COINS_PER_MOB_LEVEL = "COINS_PER_MOB_LEVEL";
    public static final String DOUBLE_BITS = "DOUBLE_BITS_CHANCE";
    public static final String POTION_DURATION = "POTION_DURATION";
    public static final String BAIT_PRESERVATION = "BAIT_PRESERVATION";
    public static final String THUNDER_CHARGE = "THUNDER_CHARGE";
    public static final String POWDER_GAIN = "POWDER_GAIN";
    public static final String CHOCOLATE_PER_SECOND = "CHOCOLATE_PER_SECOND";
    public static final String POTATO_MINION_SPEED = "POTATO_MINION_SPEED";
    private static final String POTATO_MINION = "POTATO";

    private AccessoryEconomyHooks() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.forEachLoadedPlayer(AccessoryEconomyHooks::payMinute))
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

    public static double potatoMinionSpeedPercent(Instance island, String minion) {
        if (island == null || !POTATO_MINION.equals(minion)) return 0;
        double best = 0;
        for (Player viewer : island.getPlayers()) {
            if (viewer instanceof SkyBlockPlayer player)
                best = Math.max(best, AccessoryHandlerEffects.percent(player, POTATO_MINION_SPEED));
        }
        return best;
    }

    public static long powderGained(SkyBlockPlayer player, long powder) {
        return Math.round(powder * (1 + AccessoryHandlerEffects.percent(player, POWDER_GAIN) / 100D));
    }

    public static double chocolatePerSecond(SkyBlockPlayer player) {
        return AccessoryHandlerEffects.percent(player, CHOCOLATE_PER_SECOND);
    }

    public static double thunderChargeBonus(SkyBlockPlayer player) {
        return AccessoryHandlerEffects.percent(player, THUNDER_CHARGE) / 100D;
    }
}
