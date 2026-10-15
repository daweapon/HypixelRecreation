package dev.daweapon.type.kuudra.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import dev.daweapon.type.kuudra.fight.KuudraFight;
import dev.daweapon.type.kuudra.fight.world.Displays;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One of Kuudra's tentacles (research K1 3.7): rises out of the lava at the platform edge, sways, and every so
 * often slams a telegraphed strip of the platform. An invisible Giant is the hitbox; magma cubes draw it.
 * Killing one is worth tokens; another rises later.
 */
public final class KuudraTentacle extends KuudraMob {
    public static final String ID = "KUUDRA_TENTACLE";
    private static final int[] SIZES = {16, 15, 13, 11, 10, 9, 9, 8, 7, 6, 5, 4, 4, 3, 2, 2};
    private static final double[] HEIGHTS = {0.0, 5.3, 10.1, 14.3, 17.9, 21.0, 23.8, 26.6, 29.2, 31.6, 33.4, 35.0, 36.2, 37.6, 38.6, 39.4};
    private static final double TIP_LEAN = 7.4;

    private final List<Segment> segments = new ArrayList<>();
    private int slamCountdown;

    public KuudraTentacle(KuudraFight fight, KuudraMobSpec spec) {
        super(fight, spec, EntityType.GIANT, MobType.INFERNAL, MobType.MAGMATIC);
        setInvisible(true);
        setNoGravity(true);
        slamCountdown = fight.spec().tentacles().slamIntervalSeconds() * 20 / 2
                + ThreadLocalRandom.current().nextInt(fight.spec().tentacles().slamIntervalSeconds() * 10 + 1);
    }

    public static KuudraMobSpec spec(KuudraTier tier, double health, double slamDamage, int tokens) {
        return new KuudraMobSpec(ID, "Kuudra Tentacle", tokens, 0, "KUUDRA", 0, 0,
                Map.of(tier, new KuudraMobSpec.Stats(0, (long) health, (long) slamDamage, 0, 0, 0)));
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of();
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of();
    }

    @Override
    public float getNameDisplayHeightOffset() {
        return 1.5f;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    protected void behave(int ticks) {
        drawSegments(ticks);
        if (--slamCountdown > 0) return;
        slamCountdown = fight.spec().tentacles().slamIntervalSeconds() * 20;
        fight.slam(this);
    }

    /** Like Hypixel's (the Burning capture), a tentacle is a chain of shrinking magma cubes. */
    private void drawSegments(int ticks) {
        if (getInstance() == null) return;
        while (segments.size() < SIZES.length) {
            int size = SIZES[segments.size()];
            Segment cube = new Segment(fight, spec(), this, size);
            cube.setInstance(getInstance(), getPosition());
            segments.add(cube);
        }
        Pos base = getPosition();
        Vec outward = base.sub(fight.arena().centre()).asVec().withY(0);
        outward = outward.lengthSquared() < 1e-4 ? new Vec(1, 0, 0) : outward.normalize();
        for (int i = 0; i < segments.size(); i++) {
            double fraction = (double) i / (SIZES.length - 1);
            double lean = TIP_LEAN * fraction * fraction;
            double sway = Math.sin(ticks * 0.1 + i * 0.4) * 2.7 * fraction;
            segments.get(i).teleport(base.add(outward.x() * lean - outward.z() * sway, HEIGHTS[i], outward.z() * lean + outward.x() * sway));
        }
    }

    /** One drawn cube of the chain: hits on it count as hits on the tentacle. */
    private static final class Segment extends KuudraMob {
        private final KuudraTentacle owner;

        Segment(KuudraFight fight, KuudraMobSpec spec, KuudraTentacle owner, int size) {
            super(fight, spec, EntityType.MAGMA_CUBE, MobType.INFERNAL, MobType.MAGMATIC);
            this.owner = owner;
            setNoGravity(true);
            setCustomNameVisible(false);
            editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(size));
        }

        @Override
        public List<GoalSelector> getGoalSelectors() {
            return List.of();
        }

        @Override
        public List<TargetSelector> getTargetSelectors() {
            return List.of();
        }

        @Override
        public boolean hasCollision() {
            return false;
        }

        @Override
        public boolean damage(@org.jetbrains.annotations.NotNull net.minestom.server.entity.damage.Damage damage) {
            return owner.damage(damage);
        }
    }

    @Override
    public void remove() {
        segments.forEach(Displays::remove);
        segments.clear();
        super.remove();
    }
}
