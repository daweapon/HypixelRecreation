package dev.daweapon.type.crystalhollows.worm;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobScatha;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.MobStoneworm;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbilities;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Breaking Hard Stone has a 1 in 600 chance to call a worm, then a 30 second cooldown for that player. One worm in
 * five is a Scatha. Tunnel Vision and the Treasure of the Earth perk each add to the chance as a percentage of it.
 * The Blazing reforge's Heat bonus waits for the Heat mechanic.
 */
public final class WormSpawning {
    public static final double SPAWN_CHANCE_PERCENT = 100.0 / 600;
    public static final double SCATHA_SHARE_PERCENT = 20;
    public static final long COOLDOWN_MILLIS = 30_000;
    private static final String TREASURE_PERK = "treasures_of_the_earth";
    private static final Map<UUID, Long> NEXT_ALLOWED = new ConcurrentHashMap<>();

    private WormSpawning() {
    }

    public static double chancePercent(SkyBlockPlayer player) {
        double bonusPercent = HotmAbilities.tunnelVisionBonusPercent(player) + treasureOfTheEarthPercent(player);
        return SPAWN_CHANCE_PERCENT * (1 + bonusPercent / 100);
    }

    private static double treasureOfTheEarthPercent(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.GOLD)
                .flatMap(shop -> shop.perks().stream().filter(perk -> TREASURE_PERK.equals(perk.id())).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }

    public static boolean onHardStoneBroken(SkyBlockPlayer player, Instance instance, Point point) {
        long now = System.currentTimeMillis();
        if (NEXT_ALLOWED.getOrDefault(player.getUuid(), 0L) > now) return false;
        if (!LootRoll.roll(player.getUuid(), null, chancePercent(player))) return false;
        NEXT_ALLOWED.put(player.getUuid(), now + COOLDOWN_MILLIS);
        SkyBlockMob worm = LootRoll.roll(player.getUuid(), null, SCATHA_SHARE_PERCENT) ? new MobScatha() : new MobStoneworm();
        worm.setInstance(instance, new Pos(point.x() + 0.5, point.y(), point.z() + 0.5));
        player.sendMessage("<7>You hear the sound of something approaching...");
        return true;
    }
}
