package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Where dungeon class passives reach into SkyBlock combat. The Catacombs server installs a {@link Handler}; every other
 * server keeps the neutral default, so these calls change nothing outside a run.
 */
public final class DungeonClassHooks {
    public interface Handler {
        default double meleeMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
            return 1;
        }

        default void meleeHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        }

        default double arrowMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
            return 1;
        }

        default void arrowHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        }

        default int extraArrows(SkyBlockPlayer player) {
            return 0;
        }

        default float incomingDamage(SkyBlockPlayer player, SkyBlockMob mob, float damage) {
            return damage;
        }

        default int cooldownTicks(SkyBlockPlayer player, int ticks) {
            return ticks;
        }
    }

    private static final Handler NONE = new Handler() {
    };
    private static volatile Handler handler = NONE;

    private DungeonClassHooks() {
    }

    public static void install(Handler installed) {
        handler = installed == null ? NONE : installed;
    }

    public static double meleeMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        return handler.meleeMultiplier(player, mob);
    }

    public static void meleeHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        handler.meleeHit(player, mob, damage);
    }

    public static double arrowMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        return handler.arrowMultiplier(player, mob);
    }

    public static void arrowHit(SkyBlockPlayer player, SkyBlockMob mob, double damage) {
        handler.arrowHit(player, mob, damage);
    }

    public static int extraArrows(SkyBlockPlayer player) {
        return handler.extraArrows(player);
    }

    public static float incomingDamage(SkyBlockPlayer player, SkyBlockMob mob, float damage) {
        return handler.incomingDamage(player, mob, damage);
    }

    public static int cooldownTicks(SkyBlockPlayer player, int ticks) {
        return handler.cooldownTicks(player, ticks);
    }
}
