package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.TargetSelector;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;

public class KalhuikiTarget extends TargetSelector {
    private static final double RANGE = 16;
    private final KalhuikiMob mob;

    public KalhuikiTarget(KalhuikiMob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public Entity findTarget() {
        if (mob.getInstance() == null) return null;
        return mob.getInstance().getNearbyEntities(mob.getPosition(), RANGE).stream()
                .filter(entity -> entity instanceof SkyBlockPlayer player && !player.isDead() && mob.isAngryAt(player))
                .min(Comparator.comparingDouble(entity -> entity.getDistanceSquared(mob)))
                .orElse(null);
    }
}
