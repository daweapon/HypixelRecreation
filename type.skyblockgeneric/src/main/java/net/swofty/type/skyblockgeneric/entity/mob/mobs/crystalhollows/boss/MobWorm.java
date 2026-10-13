package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A Crystal Hollows worm: only the head can be hurt, and every hit costs it one health. The body is a chain of
 * silverfish that follows the head's trail. The body length, spacing and lifetime are estimates.
 */
public abstract class MobWorm extends CrystalHollowsBossMob {
    private static final int SEGMENTS = 6;
    private static final int SEGMENT_SPACING = 4;
    private static final int LIFETIME_TICKS = 20 * 90;

    private final Deque<Pos> trail = new ArrayDeque<>();
    private final List<Entity> body = new ArrayList<>();

    protected MobWorm(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected float hitDamage() {
        return 1;
    }

    @Override
    protected double lootShareDamage() {
        return 1;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        for (int i = 0; i < SEGMENTS; i++) {
            Entity segment = new Entity(EntityType.SILVERFISH);
            segment.setNoGravity(true);
            segment.setInstance(getInstance(), getPosition());
            body.add(segment);
        }
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks > LIFETIME_TICKS) {
            remove();
            return;
        }
        trail.addFirst(getPosition());
        while (trail.size() > (SEGMENTS + 1) * SEGMENT_SPACING) trail.removeLast();
        List<Pos> positions = new ArrayList<>(trail);
        for (int i = 0; i < body.size(); i++) {
            int index = Math.min((i + 1) * SEGMENT_SPACING, positions.size() - 1);
            body.get(i).teleport(positions.get(index));
        }
    }

    @Override
    protected void onDefeated() {
        removeBody();
    }

    @Override
    public void remove() {
        removeBody();
        super.remove();
    }

    private void removeBody() {
        body.forEach(segment -> {
            if (!segment.isRemoved()) segment.remove();
        });
        body.clear();
    }
}
