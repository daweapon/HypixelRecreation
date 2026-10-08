package net.swofty.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.text.Text;
import net.swofty.type.crimsonisle.dojo.DojoArena;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Control: a Wither Skeleton paces a ring around the arena edge and sometimes leaps to a new
 * spot with a sound cue. Points accrue while you stare at it, faster the more accurately you do;
 * looking away for two seconds, or falling, ends the test.
 */
public class ControlChallenge extends DojoChallenge {
    private static final double PLATFORM_RADIUS = 3.5;
    private static final double RING_RADIUS = 13;
    private static final double GAZE_CONE_DEGREES = 10;
    private static final double MAX_POINTS_PER_TICK = 10.0 / TICKS_PER_SECOND;
    private static final int LOOK_AWAY_LIMIT = 2 * TICKS_PER_SECOND;

    private EntityCreature skeleton;
    private double angle = Math.PI / 2;
    private double direction = 1;
    private long nextTurnAt = 4 * TICKS_PER_SECOND;
    private long nextLeapAt = 8 * TICKS_PER_SECOND;
    private int lookingAwayTicks;

    public ControlChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.CONTROL);
    }

    @Override
    protected void build() {
        arena.disc(PLATFORM_RADIUS, Block.STONE_BRICKS);
        int cx = (int) Math.floor(DojoArena.CENTER_X);
        int cz = (int) Math.floor(DojoArena.CENTER_Z);
        for (int step = 0; step < 360; step++) {
            double radians = Math.toRadians(step);
            arena.floor((int) Math.floor(cx + 0.5 + Math.cos(radians) * RING_RADIUS),
                    (int) Math.floor(cz + 0.5 + Math.sin(radians) * RING_RADIUS), Block.POLISHED_BLACKSTONE_BRICKS);
        }
        skeleton = track(new EntityCreature(EntityType.WITHER_SKELETON));
        skeleton.setNoGravity(true);
        skeleton.setCustomName(Text.of("<c>Stare at me!").asComponent());
        skeleton.setCustomNameVisible(true);
        skeleton.setInstance(DojoArena.instance(), ringPosition());
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected void tick() {
        moveSkeleton();
        double offAngle = gazeOffset();
        if (offAngle <= GAZE_CONE_DEGREES) {
            lookingAwayTicks = 0;
            addScore(MAX_POINTS_PER_TICK * (1 - offAngle / GAZE_CONE_DEGREES));
        } else if (++lookingAwayTicks >= LOOK_AWAY_LIMIT) {
            fail();
        } else if (lookingAwayTicks % 10 == 1) {
            sound("block.note_block.bass", 0.6f);
        }
    }

    private void moveSkeleton() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double speed = Math.min(0.06, 0.015 + seconds() * 0.0003);
        if (ticks >= nextTurnAt) {
            direction = -direction;
            nextTurnAt = ticks + random.nextInt(3 * TICKS_PER_SECOND, 7 * TICKS_PER_SECOND);
        }
        if (ticks >= nextLeapAt) {
            angle += random.nextDouble(Math.PI / 3, Math.PI);
            nextLeapAt = ticks + random.nextInt(5 * TICKS_PER_SECOND, 10 * TICKS_PER_SECOND);
            sound("entity.enderman.teleport", 1.2f);
        } else {
            angle += speed * direction;
        }
        skeleton.teleport(ringPosition());
    }

    private Pos ringPosition() {
        double x = DojoArena.CENTER_X + Math.cos(angle) * RING_RADIUS;
        double z = DojoArena.CENTER_Z + Math.sin(angle) * RING_RADIUS;
        float yaw = (float) Math.toDegrees(Math.atan2(-(DojoArena.CENTER_X - x), DojoArena.CENTER_Z - z));
        return new Pos(x, DojoArena.STAND_Y, z, yaw, 0);
    }

    private double gazeOffset() {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec toSkeleton = skeleton.getPosition().add(0, 1.6, 0).sub(eye).asVec();
        if (toSkeleton.length() < 1e-3) return 0;
        double cosine = eye.direction().normalize().dot(toSkeleton.normalize());
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cosine))));
    }
}
