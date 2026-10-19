package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.List;

public class MobSeer extends EndMob {
    private static final int SHOT_INTERVAL = 40;
    private static final double SHOT_RANGE = 16;
    private static final double ARROW_SPEED = 1.4;

    public MobSeer() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.SEER;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new RandomRegionStrollGoal(this, spec().strollRadius(), homeRegion()));
    }

    @Override
    protected List<EndMobSpecs.Extra> extras() {
        return EndMobSpecs.SEER_EXTRAS;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % SHOT_INTERVAL != 0) return;
        nearestPlayer(SHOT_RANGE).ifPresent(target -> {
            lookAt(target);
            MobProjectile.arrow(this, target, ARROW_SPEED, hit -> MobAttackService.strike(this, hit));
        });
    }

    @Override
    protected void onDeath(Instance instance, Pos position) {
        if (instance == null) return;
        new MobEndermiteHigh().setInstance(instance, position);
    }

}
