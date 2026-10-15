package dev.daweapon.type.kuudra.fight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraStage {
    private final KuudraRun run;
    private final KuudraArena arena;
    private final Instance instance;

    KuudraStage(KuudraFight fight) {
        this.run = fight.run;
        this.arena = fight.arena;
        this.instance = fight.instance;
    }

    boolean floats(Pos position) {
        return instance.getBlock(position.blockX(), arena.lavaSurfaceY() - 1, position.blockZ()).compare(Block.LAVA);
    }

    Pos centre() {
        return arena.centre();
    }

    double loopRadius() {
        KuudraArena.Box platform = arena.platform();
        return Math.max(platform.maxX() - platform.minX(), platform.maxZ() - platform.minZ()) / 2.0 + 4;
    }

    /** A random spot on the main platform's floor at least {@code inset} blocks in from the bounding box's edge. */
    Pos randomPlatformPoint(double inset) {
        KuudraArena.Box platform = arena.platform();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 24; attempt++) {
            double x = random.nextDouble(platform.minX() + inset, platform.maxX() + 1 - inset);
            double z = random.nextDouble(platform.minZ() + inset, platform.maxZ() + 1 - inset);
            Pos at = new Pos(x, arena.floorY(), z);
            if (onMainPlatform(at)) return at;
        }
        return arena.respawn();
    }

    /** True when a straight walk from the Ballista to here never leaves solid floor: not a rock out in the lava. */
    boolean onMainPlatform(Pos at) {
        Pos centre = centre();
        Vec out = new Vec(at.x() - centre.x(), 0, at.z() - centre.z());
        double length = out.length();
        if (length < 0.5) return solidFloor(at);
        Vec step = out.normalize();
        for (double distance = 0; distance <= length; distance += 0.5) {
            if (!solidFloor(centre.add(step.mul(distance)))) return false;
        }
        return solidFloor(at);
    }

    private boolean solidFloor(Pos at) {
        Block floor = instance.getBlock((int) Math.floor(at.x()), arena.floorY() - 1, (int) Math.floor(at.z()));
        return floor.isSolid() && !floor.compare(Block.LAVA);
    }

    /** A captured spot in the lava past the platform edge where a minion surfaces (the captures). */
    Pos minionSpawn() {
        List<Pos> spawns = arena.minionSpawns();
        if (spawns.isEmpty()) return lavaNear(edgePoint());
        Pos at = spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
        return at.withY(arena.lavaSurfaceY() - 0.5);
    }

    /** Where a minion leaving the lava at {@code from} lands: a few blocks in from the main platform's edge. */
    Pos landingFor(Pos from) {
        Pos centre = centre();
        Vec out = new Vec(from.x() - centre.x(), 0, from.z() - centre.z());
        double length = out.length();
        if (length < 1) return centre;
        Vec step = out.normalize();
        double edge = 0;
        for (double distance = 0; distance <= length; distance += 0.5) {
            if (!solidFloor(centre.add(step.mul(distance)))) break;
            edge = distance;
        }
        double inset = ThreadLocalRandom.current().nextDouble(2, 5);
        return centre.add(step.mul(Math.max(0, edge - inset))).withY(arena.floorY());
    }

    Pos scatter(Pos around, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 6; attempt++) {
            Pos at = around.add(random.nextDouble(-radius, radius), 0, random.nextDouble(-radius, radius));
            if (onMainPlatform(at)) return at;
        }
        return around;
    }

    /** Minions jump out of the lava onto the platform; this is the lava they leave (layouts without captured spots). */
    private Pos lavaNear(Pos onPlatform) {
        Vec out = onPlatform.sub(centre()).asVec().withY(0);
        if (out.lengthSquared() < 1e-4) return onPlatform;
        Vec step = out.normalize();
        for (int distance = 1; distance <= 12; distance++) {
            Pos probe = onPlatform.add(step.mul(distance));
            if (floats(probe)) return probe.withY(arena.lavaSurfaceY() - 0.5);
        }
        return onPlatform;
    }

    /** A point a few blocks in from a random edge of the platform's bounding box. */
    Pos edgePoint() {
        KuudraArena.Box platform = arena.platform();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 12; attempt++) {
            double in = random.nextDouble(3, 8);
            double along = random.nextDouble();
            double x;
            double z;
            switch (random.nextInt(4)) {
                case 0 -> { x = platform.minX() + in; z = platform.minZ() + along * (platform.maxZ() - platform.minZ()); }
                case 1 -> { x = platform.maxX() + 1 - in; z = platform.minZ() + along * (platform.maxZ() - platform.minZ()); }
                case 2 -> { z = platform.minZ() + in; x = platform.minX() + along * (platform.maxX() - platform.minX()); }
                default -> { z = platform.maxZ() + 1 - in; x = platform.minX() + along * (platform.maxX() - platform.minX()); }
            }
            Pos at = new Pos(x, arena.floorY(), z);
            if (onMainPlatform(at)) return at;
        }
        return randomPlatformPoint(4);
    }

    void ring(Pos at, double radius, Particle particle) {
        int points = Math.max(8, (int) (radius * 8));
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            instance.sendGroupedPacket(new ParticlePacket(particle, at.add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius),
                    Vec.ZERO, 0, 1));
        }
    }

    void playAt(Pos at, String sound, float pitch) {
        instance.playSound(Sound.sound(Key.key(sound), Sound.Source.HOSTILE, 1f, pitch), at.x(), at.y(), at.z());
    }

    void playAll(String sound, float pitch) {
        run.onlineMembers().forEach(player -> player.playSound(Sound.sound(Key.key(sound), Sound.Source.PLAYER, 1f, pitch)));
    }

    static double horizontal(Pos a, Pos b) {
        return Math.hypot(a.x() - b.x(), a.z() - b.z());
    }
}
