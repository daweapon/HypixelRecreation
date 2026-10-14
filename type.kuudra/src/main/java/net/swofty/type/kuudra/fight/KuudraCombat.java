package net.swofty.type.kuudra.fight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.kuudra.fight.world.EnergizedChunk;
import net.swofty.type.kuudra.mob.KuudraMob;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraCombat {
    private static final int LAIR_DEATH_BLASTS = 7;
    private static final int CHUNK_EVERY_WAVES = 4;
    private static final int CHUNKS_PER_EVENT = 4;
    private static final double CHUNK_KUUDRA_PERCENT = 1.0;

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraConfig config;
    private final KuudraFightSpec spec;
    private final KuudraDialogue dialogue;
    private final Instance instance;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final KuudraBossState bossState;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    final List<EnergizedChunk> chunks = new CopyOnWriteArrayList<>();
    private final Map<UUID, Long> futileReady = new ConcurrentHashMap<>();

    KuudraCombat(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.tier = fight.tier;
        this.config = fight.config;
        this.spec = fight.spec;
        this.dialogue = fight.dialogue;
        this.instance = fight.instance;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.bossState = fight.bossState;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    boolean hitsKuudra(Pos position) {
        if (fight.boss == null) return false;
        Pos base = fight.boss.base();
        double half = fight.boss.width() / 2;
        return Math.abs(position.x() - base.x()) <= half + 0.5 && Math.abs(position.z() - base.z()) <= half + 0.5
                && position.y() >= base.y() - 0.5 && position.y() <= base.y() + fight.boss.height() + 0.5;
    }

    /** Staff damage in percent of Kuudra's health. */
    void damageKuudra(double percent) {
        synchronized (run) {
            damageKuudraPoints(bossState.maxHealth() * percent / 100);
        }
    }

    void damageKuudraPoints(double points) {
        if (fight.finishing) return;
        bossState.damage(points);
        afterDamage();
    }

    /** At 0 he dies (or, in the lair, goes down for good); on Infernal 25% opens the lair. */
    void afterDamage() {
        if (fight.finishing || fight.stopped) return;
        if (bossState.health() <= 0) {
            if (fight.lair != null && fight.lair.entered()) lairKilled();
            else beginDeath();
            return;
        }
        if (fight.lair != null && !fight.lair.started() && bossState.atFloor()) fight.lair.begin();
    }

    /** Elle's three lines while he sinks, then KUUDRA DOWN (the Burning capture: 15.5 s). */
    private void beginDeath() {
        fight.finishing = true;
        closeStomach();
        bossState.setCycling(false);
        bossState.sink();
        fight.deathFinale = KuudraFinale.death(spec.death());
    }

    private void lairKilled() {
        fight.finishing = true;
        if (fight.boss != null) {
            for (int i = 0; i < LAIR_DEATH_BLASTS; i++) {
                instance.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.HOSTILE, 1.5f, 0.49f),
                        fight.boss.base().x(), fight.boss.base().y(), fight.boss.base().z());
            }
        }
        if (fight.lair != null) fight.lair.stop();
        bossState.setCycling(false);
        bossState.sink();
        fight.later(spec.death().lairFinishTicks(), () -> run.finish(net.swofty.type.kuudra.run.KuudraRunState.Outcome.WIN));
    }

    void closeStomach() {
        if (fight.stunCycle != null) fight.stunCycle.close();
    }

    boolean arrowHitChunk(SkyBlockPlayer player, Entity target) {
        synchronized (run) {
            for (EnergizedChunk chunk : chunks) {
                if (!chunk.owns(target)) continue;
                chunk.hit(player);
                return true;
            }
            return false;
        }
    }

    void spawnChunks() {
        dialogue.chat("energized_chunks", ThreadLocalRandom.current()).forEach(run::broadcast);
        for (int i = 0; i < CHUNKS_PER_EVENT; i++) {
            chunks.add(new EnergizedChunk(instance, stage.randomPlatformPoint(10)));
        }
    }

    static boolean chunkWave(int number) {
        return number % CHUNK_EVERY_WAVES == CHUNK_EVERY_WAVES - 1;
    }

    void tickChunks() {
        for (EnergizedChunk chunk : chunks) {
            chunk.tick();
            Pos at = chunk.position();
            if (hitsKuudra(at) || hitsKuudra(at.add(0, 2, 0))) {
                chunkHitsKuudra(chunk);
            } else if (!stage.onMainPlatform(at)) {
                stage.ring(at, 1.5, Particle.LAVA);
                stage.playAt(at, "block.fire.extinguish", 1f);
            } else if (chunk.expired()) {
                chunkExplodes(chunk);
            } else {
                continue;
            }
            chunk.remove();
            chunks.remove(chunk);
        }
    }

    private void chunkHitsKuudra(EnergizedChunk chunk) {
        stage.ring(chunk.position(), 3, Particle.EXPLOSION_EMITTER);
        stage.playAt(chunk.position(), "entity.generic.explode", 0.8f);
        if (!bossState.canBeShot()) return;
        double bomberman = chunk.lastHitter() == null ? 0 : perks.fraction(chunk.lastHitter().getUuid(), "BOMBERMAN");
        damageKuudraPoints(bossState.maxHealth() * CHUNK_KUUDRA_PERCENT / 100 * (1 + bomberman));
    }

    private void chunkExplodes(EnergizedChunk chunk) {
        KuudraMobSpec landmine = config.mob("KUUDRA_LANDMINE");
        KuudraMob source = mobs.stream().findFirst().orElse(null);
        if (landmine == null || source == null || landmine.stats(tier) == null) {
            stage.ring(chunk.position(), spec.mobs().explosionRadius(), Particle.EXPLOSION);
            return;
        }
        fight.minions.explode(chunk.position(), spec.mobs().explosionRadius() * 1.5, landmine.stats(tier).damage() * 2, source);
    }

    void hitKuudra(SkyBlockPlayer player) {
        weaponHit(player, PlayerStatistics.runPrimaryDamageFormula(player.getStatistics().allStatistics(),
                ItemStatistics.builder().build()).getKey(), false);
    }

    /** An arrow (or a Salvation beam) from the player landed on Kuudra; each one is its own hit. */
    void arrowHitKuudra(SkyBlockPlayer player, double damage) {
        synchronized (run) {
            if (!fight.stopped) weaponHit(player, damage, true);
        }
    }

    /** A melee or bow hit: real damage in the lair, a counted hit while stunned, otherwise Elle's "futile" line. */
    private void weaponHit(SkyBlockPlayer player, double damage, boolean arrow) {
        if (lives.isGhost(player.getUuid()) || fight.finishing) return;
        if (fight.lair != null && fight.lair.entered()) {
            fight.lair.hit(player, damage * CombatPetHooks.kuudraDamageMultiplier(player));
            return;
        }
        if (fight.stunCycle != null && fight.stunCycle.stunnedHit(player, arrow)) return;
        if (spec.boss().playerHitsDamage()) return;
        long ready = futileReady.getOrDefault(player.getUuid(), 0L);
        if (fight.tick < ready) return;
        futileReady.put(player.getUuid(), fight.tick + spec.boss().futileCooldownSeconds() * 20L);
        dialogue.chat("futile", ThreadLocalRandom.current()).forEach(player::sendMessage);
    }

    void humanCannonball(SkyBlockPlayer player) {
        if (lives.isGhost(player.getUuid()) || !perks.of(player.getUuid()).useHumanCannonball()) return;
        stage.playAt(player.getPosition(), "entity.generic.explode", 1f);
        if (fight.stunCycle != null && fight.stunCycle.fireIntoMouth(player)) return;
        Vec look = player.getPosition().direction().normalize();
        player.setVelocity(look.mul(35).withY(22));
        player.sendMessage("<a>You were fired out of the cannon!");
    }
}
