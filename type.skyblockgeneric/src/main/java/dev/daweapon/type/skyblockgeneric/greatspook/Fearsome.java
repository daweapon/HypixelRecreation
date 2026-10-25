package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

public final class Fearsome {
    private static final double FLEE_RANGE = 8;
    private static final double FLEE_DISTANCE = 10;
    private static final int CHECK_INTERVAL_TICKS = 10;

    private Fearsome() {
    }

    public static boolean frightens(Integer mobLevel, SkyBlockPlayer player) {
        return GreatSpookEvent.isActive() && GreatSpookFear.wearsFullSet(player)
                && GreatSpookFear.frightens(mobLevel, GreatSpookFear.fear(player));
    }

    public static void tick(SkyBlockMob mob) {
        if (mob.getAliveTicks() % CHECK_INTERVAL_TICKS != 0 || !GreatSpookEvent.isActive()) return;
        if (mob.getInstance() == null || mob.getLevel() == null || mob instanceof dev.daweapon.type.skyblockgeneric.greatspook.mob.PrimalMob) return;
        Pos position = mob.getPosition();
        for (Entity nearby : mob.getInstance().getNearbyEntities(position, FLEE_RANGE)) {
            if (!(nearby instanceof SkyBlockPlayer player) || !frightens(mob.getLevel(), player)) continue;
            double dx = position.x() - player.getPosition().x();
            double dz = position.z() - player.getPosition().z();
            double length = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
            mob.getVanillaNavigator().pathTo(position.add(dx / length * FLEE_DISTANCE, 0, dz / length * FLEE_DISTANCE));
            return;
        }
    }
}
