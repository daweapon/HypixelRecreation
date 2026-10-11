package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.swofty.type.skyblockgeneric.entity.mob.MobStatusEffects;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.slayer.SlayerTier;
import net.swofty.type.skyblockgeneric.slayer.inferno.DaggerVengeance;
import net.swofty.type.skyblockgeneric.slayer.inferno.HellionAttunement;
import net.swofty.type.skyblockgeneric.slayer.inferno.HellionShield;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Quazii and Typhoeus, the demons the Inferno Demonlord splits into. One demon is active at a time
 * and the other is immune. At Tier I the second becomes active when the first dies; from Tier II both
 * carry their own Hellion Shields and swap every 8 matching hits. The Demonlord reforms when both die.
 */
final class InfernoDemons {
    private static final double[] HEALTH = {500_000, 1_750_000, 5_000_000, 10_000_000};
    private static final double[] DAMAGE = {2_500, 8_000, 12_000, 25_000};
    private static final double ROFLCOPTER_DAMAGE = 1_500;
    private static final double ROFLCOPTER_HEAL = 0.2;
    private static final double ROFLCOPTER_LENGTH = 5;
    private static final double TRAIL_DAMAGE = 1_350;
    private static final int TRAIL_LIFETIME_TICKS = 60;
    private static final double SCORCHED_ANGER_RADIUS = 4;

    private final InfernoDemonlordAbility ability;
    private final SlayerBossMob boss;
    private final SlayerTier tier;
    private final Runnable onReform;
    private final Demon quazii;
    private final Demon typhoeus;
    private Demon activeDemon;
    private boolean active;
    private double roflAngle;
    private final List<TrailPoint> trail = new ArrayList<>();
    private final Map<UUID, Long> lastBeamHit = new HashMap<>();

    InfernoDemons(InfernoDemonlordAbility ability, SlayerBossMob boss, SlayerTier tier, Runnable onReform) {
        this.ability = ability;
        this.boss = boss;
        this.tier = tier;
        this.onReform = onReform;
        this.quazii = new Demon("Quazii", EntityType.WITHER_SKELETON, List.of(HellionAttunement.SPIRIT, HellionAttunement.CRYSTAL));
        this.typhoeus = new Demon("Typhoeus", EntityType.ZOMBIFIED_PIGLIN, List.of(HellionAttunement.ASHEN, HellionAttunement.AURIC));
    }

    boolean active() {
        return active;
    }

    void spawn() {
        Instance instance = boss.getInstance();
        if (instance == null) return;
        active = true;
        activeDemon = quazii;
        quazii.spawn(instance, boss.getPosition().add(2, 0, 0));
        typhoeus.spawn(instance, boss.getPosition().add(-2, 0, 0));
        ability.every(boss, 2, 2, this::tick);
    }

    void despawn() {
        for (Demon demon : List.of(quazii, typhoeus)) {
            if (demon.mob != null && !demon.mob.isRemoved()) demon.mob.remove();
        }
        active = false;
    }

    private int index() {
        return Math.min(3, tier.number() - 1);
    }

    private void tick() {
        if (!active) return;
        if (tier.number() >= 3 && quazii.alive() && activeDemon == quazii) roflcopter();
        if (tier.number() >= 4 && typhoeus.alive() && activeDemon == typhoeus) trailOfFire();
        Demon idle = activeDemon == quazii ? typhoeus : quazii;
        SkyBlockPlayer owner = ability.ownerOf(boss);
        if (idle.alive() && owner != null && idle.mob.getPosition().distance(owner.getPosition()) < 5) {
            Pos away = idle.mob.getPosition().sub(owner.getPosition()).asVec().normalize().mul(0.6).asPosition();
            idle.mob.teleport(idle.mob.getPosition().add(away.x(), 0, away.z()));
        }
    }

    private void roflcopter() {
        roflAngle = (roflAngle + Math.PI / 20) % (Math.PI * 2);
        Pos centre = quazii.mob.getPosition().add(0, 1, 0);
        double dx = Math.cos(roflAngle);
        double dz = Math.sin(roflAngle);
        for (double d = 0.5; d <= ROFLCOPTER_LENGTH; d += 0.5) {
            particle(centre.add(dx * d, 0, dz * d), Particle.FLAME);
        }
        long now = System.currentTimeMillis();
        for (SkyBlockPlayer player : ability.fighters(boss)) {
            Pos offset = player.getPosition().sub(quazii.mob.getPosition());
            double along = offset.x() * dx + offset.z() * dz;
            double across = Math.abs(offset.x() * dz - offset.z() * dx);
            if (along < 0 || along > ROFLCOPTER_LENGTH || across > 0.8) continue;
            if (now - lastBeamHit.getOrDefault(player.getUuid(), 0L) < 1_000) continue;
            lastBeamHit.put(player.getUuid(), now);
            ability.hurt(boss, player, ROFLCOPTER_DAMAGE, "ROFLCopter", true);
            quazii.mob.setHealth((float) Math.min(quazii.mob.getMaxHealth(),
                quazii.mob.getHealth() + MobStatusEffects.healing(quazii.mob, quazii.mob.getMaxHealth() * ROFLCOPTER_HEAL)));
            quazii.mob.refreshName();
        }
    }

    private void trailOfFire() {
        long tick = typhoeus.mob.getAliveTicks();
        if (tick % 10 == 0) trail.add(new TrailPoint(typhoeus.mob.getPosition(), tick + TRAIL_LIFETIME_TICKS));
        trail.removeIf(point -> point.expiresAt < tick);
        for (TrailPoint point : trail) particle(point.pos.add(0, 0.2, 0), Particle.FLAME);
        if (tick % 20 != 0) return;
        for (SkyBlockPlayer player : ability.fighters(boss)) {
            if (trail.stream().anyMatch(point -> point.pos.distance(player.getPosition()) <= 1.2)) {
                ability.hurt(boss, player, TRAIL_DAMAGE, "Trail of Fire", false);
            }
        }
    }

    private void particle(Pos at, Particle particle) {
        Instance instance = boss.getInstance();
        if (instance == null) return;
        ParticlePacket packet = new ParticlePacket(particle, true, true, at.x(), at.y(), at.z(), 0.05f, 0.05f, 0.05f, 0f, 1);
        instance.getPlayers().forEach(player -> player.sendPacket(packet));
    }

    private void onDemonDeath(Demon demon) {
        Demon other = demon == quazii ? typhoeus : quazii;
        if (other.alive()) {
            activeDemon = other;
            ability.announce(boss, "<6>" + activeDemon.name + " <e>is now vulnerable!");
            activeDemon.mob.refreshName();
            return;
        }
        active = false;
        trail.clear();
        onReform.run();
    }

    private void swap() {
        Demon other = activeDemon == quazii ? typhoeus : quazii;
        if (!other.alive()) return;
        activeDemon = other;
        ability.announce(boss, "<6>" + other.name + " <e>takes over the fight!");
        quazii.refreshName();
        typhoeus.refreshName();
    }

    private final class Demon implements SlayerMinionMob.Hooks {
        private final String name;
        private final EntityType entityType;
        private final HellionShield shield;
        private SlayerMinionMob mob;

        private Demon(String name, EntityType entityType, List<HellionAttunement> cycle) {
            this.name = name;
            this.entityType = entityType;
            this.shield = tier.number() >= 2 ? new HellionShield(cycle, InfernoDemons.this::swap) : null;
        }

        void spawn(Instance instance, Pos at) {
            mob = SlayerMinionMob.create(boss.getOwnerUuid(), new SlayerMinionMob.SlayerMinionProfile(
                name, boss.getLevel(), entityType, HEALTH[index()], DAMAGE[index()], 120D, List.of(MobType.INFERNAL), this));
            mob.setInstance(instance, at);
        }

        boolean alive() {
            return mob != null && !mob.isDead() && !mob.isRemoved();
        }

        void refreshName() {
            if (alive()) mob.refreshName();
        }

        @Override
        public String nameHeader(SlayerMinionMob minion) {
            if (activeDemon != this) return "<7>Immune";
            return shield == null ? "<a>Vulnerable" : shield.header();
        }

        @Override
        public boolean canAttack(SlayerMinionMob minion) {
            return activeDemon == this;
        }

        @Override
        public float modifyIncomingDamage(SlayerMinionMob minion, Damage damage, float amount) {
            if (activeDemon != this) return 0F;
            if (shield == null || DaggerVengeance.isApplying()) return amount;
            float filtered = shield.filter(minion, damage, amount);
            minion.refreshName();
            return filtered;
        }

        @Override
        public void onMeleeHit(SlayerMinionMob minion, SkyBlockPlayer target) {
            if (this != typhoeus) return;
            double splash = DAMAGE[index()] / 2D;
            for (SkyBlockPlayer player : ability.fighters(boss)) {
                if (player != target && player.getPosition().distance(minion.getPosition()) <= SCORCHED_ANGER_RADIUS) {
                    ability.hurt(boss, player, splash, "Scorched Anger", true);
                }
            }
        }

        @Override
        public void onDeath(SlayerMinionMob minion) {
            onDemonDeath(this);
        }
    }

    private record TrailPoint(Pos pos, long expiresAt) {
    }
}
