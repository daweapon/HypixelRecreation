package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss.PetDropTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

abstract class MobEnderman extends EndMob {
    private static final int TELEPORT_INTERVAL = 40;
    private static final double TELEPORT_RANGE = 14;
    private static final double MIN_TELEPORT_DISTANCE = 4;
    private static final double LANDING_SPREAD = 2;

    protected MobEnderman() {
        super(EntityType.ENDERMAN);
    }

    protected abstract PetDropTable petDrops();

    @Override
    protected RegionType homeRegion() {
        return RegionType.THE_END;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % TELEPORT_INTERVAL != 0 || !(getTarget() instanceof SkyBlockPlayer target)) return;
        double distance = target.getPosition().distance(getPosition());
        if (distance < MIN_TELEPORT_DISTANCE || distance > TELEPORT_RANGE) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Pos landing = target.getPosition().add(
                (random.nextDouble() - 0.5) * 2 * LANDING_SPREAD, 0, (random.nextDouble() - 0.5) * 2 * LANDING_SPREAD);
        teleport(landing);
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer player, Instance instance, Pos position) {
        petDrops().roll(player, this);
    }
}
