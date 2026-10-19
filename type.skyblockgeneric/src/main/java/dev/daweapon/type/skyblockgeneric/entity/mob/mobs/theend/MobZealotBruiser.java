package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.theend.ZealotKills;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class MobZealotBruiser extends EndMob {
    private static final int TELEPORT_COOLDOWN_TICKS = 60;
    private static final double BEHIND_DISTANCE = 2;

    private long lastTeleportTick = Long.MIN_VALUE / 2;

    public MobZealotBruiser() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.ZEALOT_BRUISER;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    protected List<EndMobSpecs.Extra> extras() {
        return EndMobSpecs.ZEALOT_EXTRAS;
    }

    @Override
    protected void onHitBy(SkyBlockPlayer player) {
        if (getAliveTicks() - lastTeleportTick < TELEPORT_COOLDOWN_TICKS) return;
        lastTeleportTick = getAliveTicks();
        Pos at = player.getPosition();
        teleport(at.sub(at.direction().mul(BEHIND_DISTANCE)).withY(at.y()).withLookAt(at));
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer player, Instance instance, Pos position) {
        ZealotKills.onKill(player, instance, position, true);
    }
}
