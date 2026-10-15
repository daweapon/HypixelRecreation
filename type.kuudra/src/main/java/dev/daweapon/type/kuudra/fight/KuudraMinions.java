package dev.daweapon.type.kuudra.fight;

import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.kuudra.fight.world.Displays;
import dev.daweapon.type.kuudra.mob.KuudraMob;
import dev.daweapon.type.kuudra.mob.KuudraMobs;
import dev.daweapon.type.kuudra.mob.KuudraRushers;
import dev.daweapon.type.kuudra.mob.KuudraSpecialists;
import dev.daweapon.type.kuudra.mob.KuudraTentacle;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraMinions {
    private record Leap(KuudraMob mob, Pos from, Pos to, long start) {
    }

    private static final int LEAP_TICKS = 16;
    private static final Set<String> FLYERS = Set.of("WANDERING_BLAZE", "DROPSHIP");

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraConfig config;
    private final KuudraFightSpec spec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final Instance instance;
    private final KuudraTokens tokens;
    private final KuudraPerks perks;
    private final KuudraBossState bossState;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    private final List<Leap> leaps = new CopyOnWriteArrayList<>();

    KuudraMinions(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.tier = fight.tier;
        this.config = fight.config;
        this.spec = fight.spec;
        this.arena = fight.arena;
        this.dialogue = fight.dialogue;
        this.instance = fight.instance;
        this.tokens = fight.tokens;
        this.perks = fight.perks;
        this.bossState = fight.bossState;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    void spawnWave(int number) {
        synchronized (run) {
            if (fight.stopped || !fight.emerged || (fight.lair != null && fight.lair.started())) return;
            if (KuudraCombat.chunkWave(number)) fight.combat.spawnChunks();
            int partySize = Math.max(1, run.onlineMembers().size());
            for (KuudraWavePlan.Batch batch : KuudraWavePlan.plan(number, config.waves(), partySize, spec.waves())) {
                int alive = (int) mobs.stream().filter(mob -> !(mob instanceof KuudraTentacle)).count();
                int count = KuudraWavePlan.allowed(alive, spec.waves().maxAlive(), batch.count());
                if (count <= 0) continue;
                barkFor(batch.mobId(), count);
                if (batch.mobId().equals("DROPSHIP")) {
                    run.onlineMembers().forEach(player -> player.showTitle(Text.parse("<c>DROPSHIP INCOMING"), Text.empty(),
                            Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(250))));
                }
                List<KuudraSpecialists.MagmaFollower> group = new ArrayList<>();
                Pos groupFrom = stage.minionSpawn();
                Pos groupLanding = stage.landingFor(groupFrom);
                for (int i = 0; i < count; i++) {
                    String mobId = batch.mobId();
                    Pos from = mobId.equals("MAGMA_FOLLOWER") ? groupFrom : stage.minionSpawn();
                    Pos landing = switch (mobId) {
                        case "MAGMA_FOLLOWER" -> stage.scatter(groupLanding, 3);
                        case "WANDERING_BLAZE" -> fight.boss != null && bossState.stance() == KuudraBossState.Stance.SURFACED
                                ? fight.boss.mouth() : stage.landingFor(from).add(0, 12, 0);
                        case "WITHER_SENTRY" -> stage.randomPlatformPoint(8);
                        case "DROPSHIP" -> stage.centre().add(stage.loopRadius(), spec.mobs().dropshipAltitude(), 0);
                        default -> stage.landingFor(from);
                    };
                    boolean climbs = !Set.of("WANDERING_BLAZE", "DROPSHIP", "WITHER_SENTRY").contains(mobId);
                    KuudraMob mob = spawnMob(mobId, climbs ? from : landing, group);
                    if (mob instanceof KuudraSpecialists.MagmaFollower follower) group.add(follower);
                    if (mob != null && climbs) leap(mob, from, landing);
                }
            }
        }
    }

    private void barkFor(String mobId, int count) {
        String key = switch (mobId) {
            case "DROPSHIP" -> count > 1 ? "dropships" : "dropship";
            case "MAGMA_FOLLOWER" -> "magma_followers";
            case "WANDERING_BLAZE" -> "wandering_blazes";
            default -> null;
        };
        if (key != null) dialogue.chat(key, ThreadLocalRandom.current()).forEach(run::broadcast);
    }

    @Nullable KuudraMob spawnMob(String mobId, Pos at) {
        return spawnMob(mobId, at, new ArrayList<>());
    }

    private @Nullable KuudraMob spawnMob(String mobId, Pos at, List<KuudraSpecialists.MagmaFollower> group) {
        if (fight.stopped) return null;
        KuudraMobSpec mobSpec = config.mob(mobId);
        if (mobSpec == null) return null;
        KuudraMob mob = KuudraMobs.create(fight, mobSpec, group);
        if (mob == null) return null;
        mobs.add(mob);
        mob.setInstance(instance, at);
        return mob;
    }

    void forget(KuudraMob mob) {
        if (!mobs.remove(mob)) return;
        if (mob instanceof KuudraTentacle && !fight.stopped) {
            fight.tentacleOps.tentacleRespawns.add(fight.tick + spec.tentacles().respawnSeconds() * 20L);
        }
    }

    void onMobKilled(KuudraMob mob, SkyBlockPlayer killer) {
        synchronized (run) {
            tokens.gain(mob.tokens());
            double chance = perks.fraction(killer.getUuid(), "FREEZING_TOUCH");
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
                for (KuudraMob other : mobs) {
                    if (other.getPosition().distance(mob.getPosition()) <= spec.support().freezingTouchRange()) {
                        other.freeze(spec.mobs().freezeSeconds());
                    }
                }
                stage.ring(mob.getPosition(), spec.support().freezingTouchRange(), Particle.SNOWFLAKE);
            }
        }
    }

    /** Sweeping Edge: part of every hit lands on the other minions nearby. */
    void onMobHit(SkyBlockPlayer player, KuudraMob mob, double dealt) {
        double share = perks.fraction(player.getUuid(), "SWEEPING_EDGE");
        if (share <= 0) return;
        for (KuudraMob other : List.copyOf(mobs)) {
            if (other == mob || other.isDead() || other.getPosition().distance(mob.getPosition()) > spec.support().sweepingEdgeRange()) continue;
            other.damage(new EntityDamage(player, (float) (dealt * share)));
        }
    }

    /** Bonus Damage, Vulcan's Kuudra Master, and Blight Slayer's share against a red Blight. */
    double outgoingMultiplier(SkyBlockPlayer player, KuudraMob mob) {
        double multiplier = 1 + perks.fraction(player.getUuid(), "BONUS_DAMAGE") + kuudraMaster(player);
        if (mob instanceof KuudraRushers.Blight blight && blight.isRed()) multiplier *= perks.fraction(player.getUuid(), "BLIGHT_SLAYER");
        return multiplier;
    }

    private double kuudraMaster(SkyBlockPlayer player) {
        double cap = EssencePerks.value(player, CrimsonEssencePerk.KUUDRA_MASTER) / 100;
        if (cap <= 0) return 0;
        long highest = 0;
        for (KuudraTier each : KuudraTier.values()) {
            if (each.ordinal() >= tier.ordinal()) highest = Math.max(highest, KuudraProgress.highestWave(player, each));
        }
        return Math.min(cap, highest * 0.001);
    }

    void mergeMagmaFollowers(List<KuudraSpecialists.MagmaFollower> group, Pos at) {
        group.forEach(KuudraMob::remove);
        spawnMob("OMEGAGMA", at);
    }

    void explode(Pos at, double radius, double damage, KuudraMob source) {
        stage.ring(at, radius, Particle.EXPLOSION);
        stage.playAt(at, "entity.generic.explode", 1f);
        for (SkyBlockPlayer player : fight.alivePlayers()) {
            if (player.getPosition().distance(at) <= radius) MobAttackService.strike(source, player, damage);
        }
    }

    void primeTnt(Pos at, int fuseTicks, double damage, KuudraMob source) {
        Entity tnt = new Entity(EntityType.TNT);
        tnt.editEntityMeta(PrimedTntMeta.class, meta -> meta.setFuseTime(fuseTicks));
        tnt.setInstance(instance, at);
        fight.later(fuseTicks, () -> {
            Displays.remove(tnt);
            explode(at, spec.mobs().explosionRadius(), damage, source);
        });
    }

    void dropshipBomb(KuudraMob ship, double damage) {
        stage.ring(stage.centre(), 6, Particle.EXPLOSION_EMITTER);
        stage.playAt(stage.centre(), "entity.generic.explode", 0.6f);
        for (SkyBlockPlayer player : fight.alivePlayers()) MobAttackService.strike(ship, player, damage);
    }

    /** Minions jump out of the lava onto the platform in an arc (the captures). */
    private void leap(KuudraMob mob, Pos from, Pos to) {
        leaps.removeIf(each -> each.mob() == mob);
        mob.setNoGravity(true);
        leaps.add(new Leap(mob, from, to, fight.tick));
    }

    void tickLeaps() {
        for (Leap leap : leaps) {
            KuudraMob mob = leap.mob();
            if (mob.isDead() || mob.isRemoved()) {
                leaps.remove(leap);
                continue;
            }
            if (mob.getInstance() == null) continue;
            double t = Math.min(1, (fight.tick - leap.start()) / (double) LEAP_TICKS);
            double arc = Math.max(3, leap.to().y() - leap.from().y() + 2.5);
            Vec path = leap.to().sub(leap.from()).asVec();
            Pos at = leap.from().add(path.mul(t)).add(0, arc * 4 * t * (1 - t), 0);
            float yaw = (float) Math.toDegrees(Math.atan2(-path.x(), path.z()));
            mob.setVelocity(Vec.ZERO);
            mob.teleport(at.withView(yaw, 0));
            if (t >= 1) {
                leaps.remove(leap);
                mob.setNoGravity(false);
            }
        }
    }

    /** Minions knocked off the platform into the lava climb back out instead of piling up there. */
    void rescueFallenMobs() {
        if (fight.lair != null && fight.lair.started()) return;
        for (KuudraMob mob : mobs) {
            if (mob instanceof KuudraTentacle || mob.isDead() || FLYERS.contains(mob.spec().id())) continue;
            if (leaps.stream().anyMatch(leap -> leap.mob() == mob)) continue;
            Pos at = mob.getPosition();
            if (at.y() >= arena.floorY() - 1.5 || at.y() < arena.lavaSurfaceY() - 8) continue;
            if (!arena.sea().containsColumn(at.blockX(), at.blockZ())) continue;
            leap(mob, at, stage.landingFor(at));
        }
    }
}
