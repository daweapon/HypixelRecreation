package net.swofty.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.swofty.type.crimsonisle.dojo.DojoArena;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.crimsonisle.dojo.DojoMobs;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Tenacity: ghasts in the arena corners shoot fireballs that blast holes in the round platform
 * and knock you about. Another ghast joins every 30 seconds, up to four. Five points per second.
 */
public class TenacityChallenge extends DojoChallenge {
    private static final double PLATFORM_RADIUS = 10.5;
    private static final double POINTS_PER_TICK = 5.0 / TICKS_PER_SECOND;
    private static final int NEW_GHAST_INTERVAL = 30 * TICKS_PER_SECOND;
    private static final double FIREBALL_SPEED = 0.55;
    private static final double BLAST_RADIUS = 1.6;
    private static final Pos[] GHAST_SPOTS = {
            new Pos(-221.5, 101, -612.5), new Pos(-192.5, 101, -582.5),
            new Pos(-192.5, 101, -612.5), new Pos(-221.5, 101, -582.5)};

    private final List<Ghast> ghasts = new ArrayList<>();
    private final List<Fireball> fireballs = new ArrayList<>();

    public TenacityChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.TENACITY);
    }

    @Override
    protected void build() {
        arena.disc(PLATFORM_RADIUS, Block.POLISHED_ANDESITE);
        addGhast();
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected void tick() {
        addScore(POINTS_PER_TICK);
        if (ticks % NEW_GHAST_INTERVAL == 0 && ghasts.size() < GHAST_SPOTS.length) {
            addGhast();
            player.sendMessage("<e>The ghast is becoming more frustrated...");
        }
        for (Ghast ghast : ghasts) {
            ghast.entity.lookAt(player);
            if (ticks >= ghast.nextShotAt) shoot(ghast);
        }
        moveFireballs();
    }

    private void addGhast() {
        EntityCreature entity = track(new EntityCreature(EntityType.GHAST));
        entity.setNoGravity(true);
        entity.setInstance(DojoArena.instance(), GHAST_SPOTS[ghasts.size()]);
        ghasts.add(new Ghast(entity, ticks + 40 + ghasts.size() * 15L));
    }

    private void shoot(Ghast ghast) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ghast.nextShotAt = ticks + random.nextInt(50, 80);
        Pos from = ghast.entity.getPosition().add(0, 2, 0);
        Pos target = player.getPosition().add(random.nextDouble(-1, 1), 0.5, random.nextDouble(-1, 1));
        Vec velocity = target.sub(from).asVec().normalize().mul(FIREBALL_SPEED);
        Entity fireball = track(new Entity(EntityType.FIREBALL));
        fireball.setNoGravity(true);
        fireball.setInstance(DojoArena.instance(), from.add(velocity.mul(3)));
        fireballs.add(new Fireball(fireball, velocity, ticks + 10 * TICKS_PER_SECOND));
        sound("entity.ghast.shoot", 1f);
    }

    private void moveFireballs() {
        Iterator<Fireball> iterator = fireballs.iterator();
        while (iterator.hasNext()) {
            Fireball fireball = iterator.next();
            Pos next = fireball.entity.getPosition().add(fireball.velocity);
            boolean hitPlayer = next.distance(player.getPosition().add(0, 0.9, 0)) < 1.4;
            boolean hitFloor = next.y() <= DojoArena.FLOOR_Y + 1 || isSolid(next);
            boolean expired = ticks >= fireball.expiresAt || !DojoArena.inside(next.x(), next.z());
            if (hitPlayer || hitFloor) explode(next);
            if (hitPlayer || hitFloor || expired) {
                fireball.entity.remove();
                iterator.remove();
                continue;
            }
            fireball.entity.teleport(next);
        }
    }

    private boolean isSolid(Pos position) {
        Block block = arena.get(position.blockX(), position.blockY(), position.blockZ());
        return !block.air() && !block.compare(Block.LAVA);
    }

    private void explode(Pos at) {
        int radius = (int) Math.ceil(BLAST_RADIUS);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > BLAST_RADIUS * BLAST_RADIUS) continue;
                int x = at.blockX() + dx;
                int z = at.blockZ() + dz;
                if (arena.isPlaced(x, DojoArena.FLOOR_Y, z)) arena.set(x, DojoArena.FLOOR_Y, z, Block.LAVA);
            }
        }
        if (player.getPosition().distance(at) < 3.5) DojoMobs.knockAway(player, at, 9, 5);
        sound("entity.generic.explode", 1f);
    }

    private static final class Ghast {
        private final EntityCreature entity;
        private long nextShotAt;

        private Ghast(EntityCreature entity, long nextShotAt) {
            this.entity = entity;
            this.nextShotAt = nextShotAt;
        }
    }

    private record Fireball(Entity entity, Vec velocity, long expiresAt) {
    }
}
