package net.swofty.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.crimsonisle.dojo.DojoArena;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.crimsonisle.dojo.DojoMobs;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Force: punch zombies off a round platform into the lava, bare-fisted. Helmet decides the points
 * (iron +10, gold +20, diamond baby +30); bare-headed zombies cost 30 when you hit them. A zombie left alone
 * vanishes after 5 to 7 seconds.
 */
public class ForceChallenge extends DojoChallenge {
    private static final double PLATFORM_RADIUS = 10.5;
    private static final int MAX_ZOMBIES = 9;
    private static final int SPAWN_INTERVAL = 12;
    private static final int KNOCKBACK_STUN = 12;

    private final Map<EntityCreature, Kind> zombies = new HashMap<>();
    private final Map<EntityCreature, Long> stunnedUntil = new HashMap<>();
    private final Map<EntityCreature, Long> despawnAt = new HashMap<>();

    public ForceChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.FORCE);
    }

    @Override
    protected void build() {
        arena.disc(PLATFORM_RADIUS, Block.SMOOTH_STONE);
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected void tick() {
        if (zombies.size() < MAX_ZOMBIES && ticks % SPAWN_INTERVAL == 0) spawnZombie();

        zombies.entrySet().removeIf(entry -> {
            EntityCreature zombie = entry.getKey();
            if (zombie.isRemoved()) return forget(zombie);
            if (DojoArena.isInLava(zombie.getPosition())) {
                if (entry.getValue().points > 0) {
                    addScore(entry.getValue().points);
                    sound("entity.experience_orb.pickup", 1.4f);
                }
                zombie.remove();
                return forget(zombie);
            }
            if (ticks >= despawnAt.getOrDefault(zombie, Long.MAX_VALUE) && stunnedUntil.getOrDefault(zombie, 0L) < ticks) {
                zombie.remove();
                return forget(zombie);
            }
            if (stunnedUntil.getOrDefault(zombie, 0L) < ticks && zombie.isOnGround()) {
                DojoMobs.walkToward(zombie, player.getPosition(), 2.2, 1.3);
            }
            return false;
        });
    }

    @Override
    public void onAttack(Entity target) {
        if (!(target instanceof EntityCreature zombie) || !zombies.containsKey(zombie)) return;
        if (stunnedUntil.getOrDefault(zombie, 0L) > ticks) return;
        Kind kind = zombies.get(zombie);
        if (kind.points < 0) {
            addScore(kind.points);
            sound("entity.villager.no", 1f);
        }
        stunnedUntil.put(zombie, ticks + KNOCKBACK_STUN);
        DojoMobs.knockAway(zombie, player.getPosition(), 16, 6);
        sound("entity.zombie.hurt", 1f);
    }

    private void spawnZombie() {
        Kind kind = Kind.pick();
        EntityCreature zombie = DojoMobs.zombie(kind.helmet, kind.baby, kind.label());
        Pos at = DojoMobs.randomOnDisc(2, PLATFORM_RADIUS - 1.5);
        if (at.distance(player.getPosition()) < 2) at = DojoMobs.randomOnDisc(2, PLATFORM_RADIUS - 1.5);
        zombie.setInstance(DojoArena.instance(), at);
        zombies.put(track(zombie), kind);
        despawnAt.put(zombie, ticks + mobLifetimeTicks());
    }

    private boolean forget(EntityCreature zombie) {
        stunnedUntil.remove(zombie);
        despawnAt.remove(zombie);
        return true;
    }

    private enum Kind {
        IRON(Material.IRON_HELMET, false, 10, 40),
        GOLD(Material.GOLDEN_HELMET, false, 20, 25),
        DIAMOND(Material.DIAMOND_HELMET, true, 30, 15),
        NEGATIVE(null, false, -30, 20);

        private final Material helmet;
        private final boolean baby;
        private final int points;
        private final int weight;

        Kind(Material helmet, boolean baby, int points, int weight) {
            this.helmet = helmet;
            this.baby = baby;
            this.points = points;
            this.weight = weight;
        }

        private String label() {
            return points > 0 ? "<a>+" + points : "<c>" + points;
        }

        private static Kind pick() {
            int roll = ThreadLocalRandom.current().nextInt(100);
            for (Kind kind : values()) {
                roll -= kind.weight;
                if (roll < 0) return kind;
            }
            return IRON;
        }
    }
}
