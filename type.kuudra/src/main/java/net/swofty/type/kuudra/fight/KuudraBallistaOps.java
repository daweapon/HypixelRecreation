package net.swofty.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.kuudra.fight.world.Projectile;
import net.swofty.type.kuudra.mob.KuudraMob;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import net.swofty.type.skyblockgeneric.essence.CrimsonEssencePerk;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraBallistaOps {
    private static final int MOUNT_GRACE_TICKS = 10;

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraFightSpec spec;
    private final KuudraTierSpec tierSpec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final Instance instance;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final KuudraBossState bossState;
    private final BallistaFuel fuel;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    private final Map<UUID, Long> shotReady = new ConcurrentHashMap<>();
    private final Map<UUID, Long> freshToolsUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> freshToolsRolled = new ConcurrentHashMap<>();
    private long lastBallistaBark = Long.MIN_VALUE;

    KuudraBallistaOps(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.tier = fight.tier;
        this.spec = fight.spec;
        this.tierSpec = fight.tierSpec;
        this.arena = fight.arena;
        this.dialogue = fight.dialogue;
        this.instance = fight.instance;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.bossState = fight.bossState;
        this.fuel = fight.fuel;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    /** Sneak + left-click on a pile builds it (research K1 3.2). */
    void hitPile(String pile, SkyBlockPlayer player) {
        if (fight.build == null || run.state().phase() != KuudraPhase.BUILD || !player.isSneaking()) return;
        if (lives.isGhost(player.getUuid()) || fight.supplies.carrying.containsKey(player.getUuid())) return;
        if (KuudraStage.horizontal(player.getPosition(), arena.piles().get(pile)) > spec.build().buildRange()) return;
        if (!fight.build.click(player.getUuid(), pile, fight.tick, spec.build().clickHoldTicks())) return;
        stage.playAt(arena.piles().get(pile), "block.ladder.break", 1f);
        long rollEvery = spec.build().freshToolsRollSeconds() * 20L;
        if (fight.tick - freshToolsRolled.getOrDefault(player.getUuid(), Long.MIN_VALUE / 2) >= rollEvery) {
            freshToolsRolled.put(player.getUuid(), fight.tick);
            double chance = EssencePerks.value(player, CrimsonEssencePerk.FRESH_TOOLS) / 100;
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance
                    && freshToolsUntil.getOrDefault(player.getUuid(), 0L) < fight.tick) {
                freshToolsUntil.put(player.getUuid(), fight.tick + spec.build().freshToolsSeconds() * 20L);
                player.sendMessage(dialogue.announcement("fresh_tools", Map.of()));
            }
        }
    }

    double buildSpeed(UUID uuid) {
        SkyBlockPlayer player = fight.member(uuid);
        double organs = player == null ? 0 : KuudraSupplies.organ(player, spec.build().organBonus());
        double fresh = freshToolsUntil.getOrDefault(uuid, 0L) >= fight.tick ? 2 : 1;
        return (1 + perks.fraction(uuid, "BALLISTA_MECHANIC") + organs) * fresh;
    }

    void tickBuild() {
        long tick = fight.tick;
        if (fight.build == null || fight.ballista == null || run.state().phase() != KuudraPhase.BUILD) return;
        List<String> finished = fight.build.tick(tick, this::buildSpeed);
        finished.forEach(pile -> stage.playAt(arena.piles().get(pile), "block.anvil.use", 1.5f));
        if (tick % 10 == 0 && fight.build.helping() > 0) stage.playAt(fight.ballista.base(), "block.anvil.land", 1.7f);
        if (tick % (spec.build().idleDecaySeconds() * 20L) == 0) fight.build.decayIdle(spec.build().idleDecayProgress());
        if (tick % (spec.build().mobHitIntervalSeconds() * 20L) == 0) {
            long attackers = mobs.stream().filter(KuudraMob::attacksBallista)
                    .filter(mob -> !mob.isDead() && mob.getPosition().distance(fight.ballista.base()) <= spec.build().mobHitRange())
                    .count();
            boolean damaged = false;
            for (int i = 0; i < attackers; i++) {
                damaged |= fight.build.knockAny(spec.build().mobHitProgress(), ThreadLocalRandom.current()) != null;
            }
            if (damaged) barkBallistaDamaged();
        }
        if (tick % 5 == 0) fight.ballista.showBuild(fight.build);
        if (fight.build.complete()) {
            fight.ballista.showReady(fuel.loaded(), fuel.cells());
            run.advancePhase();
        }
    }

    void barkBallistaDamaged() {
        long cooldown = spec.build().damageBarkCooldownSeconds() * 20L;
        if (fight.tick - lastBallistaBark < cooldown) return;
        lastBallistaBark = fight.tick;
        dialogue.chat("ballista_damaged", ThreadLocalRandom.current()).forEach(run::broadcast);
    }

    void useBallistaSeat(SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.ballista == null || fight.stopped || lives.isGhost(player.getUuid()) || fight.ballista.isRiding(player)) return;
            if (fight.supplies.tryLoadFuel(player) || fight.supplies.carrying.containsKey(player.getUuid())) return;
            KuudraPhase phase = run.state().phase();
            if (phase == KuudraPhase.SUPPLIES || phase == KuudraPhase.BUILD) {
                player.sendMessage("<c>The Ballista isn't built yet!");
                return;
            }
            if (!fuel.armed()) {
                player.sendMessage("<c>The Ballista needs " + fuel.cells() + " Fuel Cells to fire! <7>(" + fuel.loaded() + "/" + fuel.cells() + ")");
                return;
            }
            if (!fight.ballista.mount(player)) {
                player.sendMessage("<c>Someone is already using the Ballista!");
                return;
            }
            shotReady.put(player.getUuid(), fight.tick + MOUNT_GRACE_TICKS);
            player.sendMessage("<a>You mounted the Ballista! <e>Look to aim, left-click <a>to fire, sneak to get off.");
        }
    }

    void fireBallista(SkyBlockPlayer player) {
        if (fight.ballista == null || fight.boss == null) return;
        long ready = shotReady.getOrDefault(player.getUuid(), 0L);
        if (fight.tick < ready) return;
        shotReady.put(player.getUuid(), fight.tick + spec.fuel().shotCooldownTicks());
        if (!fuel.armed()) {
            player.sendMessage("<c>The Ballista is out of fuel!");
            return;
        }
        if (!bossState.canBeShot()) {
            player.sendMessage("<c>Kuudra is out of reach!");
            return;
        }
        Pos muzzle = fight.ballista.muzzle();
        Vec look = player.getPosition().direction().normalize();
        Vec toKuudra = fight.boss.mouth().sub(muzzle).asVec().normalize();
        boolean onTarget = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.dot(toKuudra))))) <= spec.fuel().aimToleranceDegrees();
        Vec direction = onTarget ? toKuudra : look;
        fight.artillery.shots.add(new KuudraArtillery.Shot(new Projectile(instance, player, muzzle, direction.mul(spec.fuel().boltSpeed()),
                ItemStack.of(Material.SPECTRAL_ARROW), 2.5f, 0, 80), KuudraArtillery.ShotKind.BOLT));
        stage.playAt(muzzle, "entity.arrow.shoot", 0.6f);
        fight.supplies.spendFuel();
    }

    double ballistaDamage(SkyBlockPlayer player) {
        double damage = spec.boss().shotDamage().get(tier) * (1 + perks.fraction(player.getUuid(), "STEADY_AIM"));
        if (bossState.isStunned()) return damage * spec.stun().stunnedShotFactor();
        return tierSpec.stunRequired() ? damage * spec.boss().unstunnedShotFactor() : damage;
    }
}
