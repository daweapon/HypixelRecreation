package dev.daweapon.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.kuudra.fight.world.KuudraCannon;
import dev.daweapon.type.kuudra.fight.world.Projectile;
import dev.daweapon.type.kuudra.mob.KuudraMob;
import dev.daweapon.type.kuudra.mob.KuudraSpecialists;
import dev.daweapon.type.kuudra.mob.KuudraTentacle;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraArtillery {
    enum ShotKind {BOLT, CANNONBALL}

    record Shot(Projectile projectile, ShotKind kind) {
    }

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final Instance instance;
    private final KuudraTokens tokens;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final KuudraBossState bossState;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    final List<KuudraCannon> cannons = new ArrayList<>();
    final List<Shot> shots = new CopyOnWriteArrayList<>();
    private final Map<UUID, Long> cannonReady = new ConcurrentHashMap<>();

    KuudraArtillery(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.instance = fight.instance;
        this.tokens = fight.tokens;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.bossState = fight.bossState;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    void spawnCannons() {
        fight.arena.cannons().forEach(at -> cannons.add(new KuudraCannon(instance, at, this::useCannon)));
    }

    void tickShots() {
        for (Shot shot : shots) {
            Projectile projectile = shot.projectile();
            boolean landed = shot.kind() == ShotKind.BOLT
                    ? projectile.tick(fight.combat::hitsKuudra)
                    : projectile.tick(position -> mobAt(position) != null);
            if (!landed) continue;
            shots.remove(shot);
            if (shot.kind() == ShotKind.BOLT) landBolt(projectile);
            else landCannonball(projectile);
        }
    }

    private void landBolt(Projectile bolt) {
        if (!fight.combat.hitsKuudra(bolt.position()) || !bossState.canBeShot()) return;
        stage.ring(bolt.position(), 1.5, Particle.LAVA);
        stage.playAt(bolt.position(), "entity.generic.explode", 0.7f);
        tokens.gain(spec.boss().shotTokens());
        bossState.shoot(fight.ballistaOps.ballistaDamage(bolt.shooter()));
        fight.combat.afterDamage();
    }

    void useCannon(SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.stopped || lives.isGhost(player.getUuid()) || fight.supplies.carrying.containsKey(player.getUuid())) return;
            KuudraCannon cannon = cannons.stream().filter(each -> each.position().distance(player.getPosition()) < 6)
                    .min((a, b) -> Double.compare(a.position().distance(player.getPosition()), b.position().distance(player.getPosition())))
                    .orElse(null);
            if (cannon == null) return;
            if (cannon.isRiding(player)) return;
            if (!cannon.mount(player)) {
                player.sendMessage("<c>Someone is already using this cannon!");
                return;
            }
            run.broadcast(player.getFullDisplayName() + " <7>mounted a <f>Cannon<7>!");
            player.sendMessage("<e>Left-click <a>to fire, sneak to get out.");
        }
    }

    void fireCannon(SkyBlockPlayer player, KuudraCannon cannon) {
        UUID uuid = player.getUuid();
        double cooldownSeconds = perks.value(uuid, "RAPID_FIRE") > 0 ? perks.value(uuid, "RAPID_FIRE") : spec.cannons().cooldownSeconds();
        if (fight.tick < cannonReady.getOrDefault(uuid, 0L)) return;
        cannonReady.put(uuid, fight.tick + Math.round(cooldownSeconds * 20));
        int balls = perks.value(uuid, "MULTI_SHOT") > 0 ? (int) perks.value(uuid, "MULTI_SHOT") : 1;
        double speed = spec.cannons().speed() * (1 + perks.fraction(uuid, "ACCELERATED_SHOT"));
        Vec look = player.getPosition().direction().normalize();
        for (int i = 0; i < balls; i++) {
            Vec direction = i == 0 ? look : look.rotateAroundY((ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3)
                    .add(0, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1, 0).normalize();
            shots.add(new Shot(new Projectile(instance, player, cannon.muzzle(player), direction.mul(speed),
                    ItemStack.of(Material.COAL_BLOCK), 0.6f, 0.04, 100), ShotKind.CANNONBALL));
        }
        stage.playAt(cannon.position(), "entity.generic.explode", 0.5f);
    }

    double cannonDamage(SkyBlockPlayer player, KuudraMob target) {
        UUID uuid = player.getUuid();
        boolean proficient = perks.value(uuid, "CANNON_PROFICIENCY") > 0;
        double percent = proficient ? perks.value(uuid, "CANNON_PROFICIENCY") : spec.cannons().damagePercent();
        double flat = proficient ? perks.secondValue(uuid, "CANNON_PROFICIENCY") : spec.cannons().damageFlat();
        double damage = target.getMaxHealth() * percent / 100 + flat;
        if (target instanceof KuudraSpecialists.Dropship) damage *= spec.cannons().dropshipMultiplier();
        if (target instanceof KuudraTentacle) damage *= spec.cannons().tentacleMultiplier();
        return damage;
    }

    private void landCannonball(Projectile ball) {
        SkyBlockPlayer player = ball.shooter();
        double radius = spec.cannons().blastRadius() + perks.value(player.getUuid(), "BLAST_RADIUS");
        stage.ring(ball.position(), radius, Particle.EXPLOSION);
        stage.playAt(ball.position(), "entity.generic.explode", 1f);
        for (KuudraMob mob : List.copyOf(mobs)) {
            if (mob.isDead() || mob.getPosition().add(0, mob.getBoundingBox().height() / 2, 0).distance(ball.position())
                    > radius + mob.getBoundingBox().width()) continue;
            mob.damage(new EntityDamage(player, (float) cannonDamage(player, mob)));
        }
    }

    private @Nullable KuudraMob mobAt(Pos position) {
        for (KuudraMob mob : mobs) {
            if (mob.isDead()) continue;
            double reach = Math.max(1, mob.getBoundingBox().width());
            if (mob.getPosition().add(0, mob.getBoundingBox().height() / 2, 0).distance(position) <= reach) return mob;
        }
        return null;
    }

    void knockOffCannon(SkyBlockPlayer player) {
        cannons.forEach(cannon -> {
            if (cannon.isRiding(player)) cannon.dismount();
        });
    }
}
