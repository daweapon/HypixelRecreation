package dev.daweapon.type.kuudra.fight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.entity.ArmorStandModels;
import dev.daweapon.type.kuudra.fight.world.BallistaSite;
import dev.daweapon.type.kuudra.fight.world.Displays;
import dev.daweapon.type.kuudra.fight.world.ElleLavaRod;
import dev.daweapon.type.kuudra.fight.world.EnergizedChunk;
import dev.daweapon.type.kuudra.fight.world.KuudraBoss;
import dev.daweapon.type.kuudra.fight.world.KuudraCannon;
import dev.daweapon.type.kuudra.fight.world.KuudraItems;
import dev.daweapon.type.kuudra.fight.world.LavaCargo;
import dev.daweapon.type.kuudra.fight.world.TentacleSlams;
import dev.daweapon.type.kuudra.gui.GUIKuudraPerkShop;
import dev.daweapon.type.kuudra.mob.KuudraMob;
import dev.daweapon.type.kuudra.mob.KuudraSpecialists;
import dev.daweapon.type.kuudra.mob.KuudraTentacle;
import dev.daweapon.type.kuudra.run.KuudraPhase;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One run's Kuudra fight, Basic and Hot end to end (research K1 3): supplies, Ballista build, fuel and shooting,
 * Kuudra's rise and submerge loop, tentacles, cannons, waves of minions, tokens and perks, deaths and revives.
 * Every entry point locks on the run, so the run's state machine and the fight move together.
 */
public final class KuudraFight {
    public record Hud(int tokens, @Nullable String progress, int submergesIn, @Nullable String status,
                      boolean objective, boolean waves) {
        public Hud(int tokens, @Nullable String progress, int submergesIn, @Nullable String status) {
            this(tokens, progress, submergesIn, status, true, true);
        }
    }

    public enum StaffResult {DONE, NOT_A_STUN_TIER, NOT_INFERNAL, NOT_READY}

    final KuudraRun run;
    final KuudraTier tier;
    final KuudraConfig config;
    final KuudraFightSpec spec;
    final KuudraTierSpec tierSpec;
    final KuudraArena arena;
    final KuudraDialogue dialogue;
    final Instance instance;
    final KuudraTokens tokens;
    final KuudraPerks perks;
    final KuudraLives lives;
    final KuudraBossState bossState;
    final BallistaFuel fuel;

    final Set<KuudraMob> mobs = ConcurrentHashMap.newKeySet();
    private final List<Entity> props = new ArrayList<>();

    final KuudraStage stage;
    final KuudraSupplies supplies;
    final KuudraBallistaOps ballistaOps;
    final KuudraArtillery artillery;
    final KuudraCombat combat;
    final KuudraTentacleOps tentacleOps;
    final KuudraMinions minions;
    final KuudraDeaths deaths;
    final KuudraDisplay display;
    final KuudraInput input;
    final KuudraStaffControls staff;
    final KuudraPhases phases;

    @Nullable BallistaBuild build;
    @Nullable BallistaSite ballista;
    @Nullable KuudraBoss boss;
    @Nullable TentacleSlams slams;
    @Nullable ElleLavaRod rod;
    private @Nullable Task task;
    @Nullable KuudraStunCycle stunCycle;
    @Nullable KuudraLairPhase lair;
    @Nullable KuudraFinale deathFinale;
    boolean finishing;
    int suppliesPlaced;
    boolean emerged;
    boolean stopped;
    long tick;

    public KuudraFight(KuudraRun run, KuudraTier tier, KuudraConfig config) {
        this.run = run;
        this.tier = tier;
        this.config = config;
        this.spec = config.fight();
        this.tierSpec = config.tier(tier);
        this.arena = config.arena();
        this.dialogue = config.dialogue();
        this.instance = run.getInstance();
        this.tokens = new KuudraTokens();
        this.perks = new KuudraPerks(config.perkShop(), tokens, config.routeUnlockTokens());
        this.fuel = new BallistaFuel(spec.fuel().cells());
        this.lives = new KuudraLives(spec.lives().respawnSeconds(), spec.lives().reviveInvulnerableSeconds());
        KuudraFightSpec.Boss boss = spec.boss();
        this.bossState = new KuudraBossState(boss.health(), boss.surfaceSeconds(), boss.shotsPerSurface(),
                boss.submergedSeconds(), boss.moveTicks());
        if (tierSpec.lairPhase() && arena.lair() != null) bossState.setFloor(boss.health() * spec.lair().threshold());
        this.stage = new KuudraStage(this);
        this.supplies = new KuudraSupplies(this);
        this.ballistaOps = new KuudraBallistaOps(this);
        this.artillery = new KuudraArtillery(this);
        this.combat = new KuudraCombat(this);
        this.tentacleOps = new KuudraTentacleOps(this);
        this.minions = new KuudraMinions(this);
        this.deaths = new KuudraDeaths(this);
        this.display = new KuudraDisplay(this);
        this.input = new KuudraInput(this);
        this.staff = new KuudraStaffControls(this);
        this.phases = new KuudraPhases(this);
    }

    // ---- lifecycle -------------------------------------------------------------------------------------------

    public void start() {
        synchronized (run) {
            run.onlineMembers().forEach(this::join);
            ballista = new BallistaSite(instance, arena, supplies::usePile, ballistaOps::useBallistaSeat);
            artillery.spawnCannons();
            for (ArmorStandModels.Stand stand : ArmorStandModels.load("/kuudra_models.json", "perk_shop")) {
                LivingEntity entity = stand.create();
                entity.setInstance(instance, stand.position());
                props.add(entity);
            }
            props.add(Displays.text(instance, arena.perkShop().add(0, 1.9, 0), "<a><l>Perk Shop"));
            props.add(Displays.hitbox(instance, arena.perkShop(), 1.2f, 1.2f, this::openPerkShop));
            slams = new TentacleSlams(instance, arena.floorY() - 1, spec.tentacles().telegraphTicks());
            rod = new ElleLavaRod(instance, arena.lavaSurfaceY(), supplies::reelElleRod);
            if (tierSpec.stunRequired() && arena.stomach() != null) stunCycle = new KuudraStunCycle(this, arena.stomach());
            if (tierSpec.lairPhase() && arena.lair() != null) lair = new KuudraLairPhase(this, arena.lair());
            task = MinecraftServer.getSchedulerManager().buildTask(this::tickSafely)
                    .repeat(TaskSchedule.tick(1)).schedule();
        }
    }

    /** Elle has hooked Kuudra: he rises, his tentacles come up and the supplies are in the lava. */
    public void emerge() {
        synchronized (run) {
            if (emerged || stopped) return;
            emerged = true;
            boss = new KuudraBoss(instance, bossState, arena.kuudraSides(), arena.lavaSurfaceY(), spec.boss(),
                    ThreadLocalRandom.current());
            bossState.rise();
            for (int i = 0; i < spec.tentacles().active(tier); i++) tentacleOps.spawnTentacle();
            if (run.state().phase() == KuudraPhase.SUPPLIES) supplies.spawnCargo(LavaCargo.Kind.SUPPLIES, spec.supplies().crates());
            spawnWave(run.state().wave());
        }
    }

    public void onPhase(@Nullable KuudraPhase phase) {
        phases.onPhase(phase);
    }

    /** The run is over: the fight stops, minions vanish and the run-only items are taken back. */
    public void stop() {
        synchronized (run) {
            if (stopped) return;
            stopped = true;
            recordHighestWave();
            supplies.clearCargo();
            new ArrayList<>(supplies.carrying.keySet()).forEach(uuid -> supplies.dropCarried(uuid, false));
            new ArrayList<>(mobs).forEach(KuudraMob::remove);
            mobs.clear();
            combat.chunks.forEach(EnergizedChunk::remove);
            combat.chunks.clear();
            artillery.shots.forEach(shot -> shot.projectile().remove());
            artillery.shots.clear();
            if (slams != null) slams.clear();
            if (stunCycle != null) stunCycle.close();
            if (lair != null) lair.stop();
            if (rod != null) rod.clear();
            artillery.cannons.forEach(KuudraCannon::dismount);
            if (ballista != null) ballista.dismount();
            for (SkyBlockPlayer player : run.onlineMembers()) {
                if (lives.isGhost(player.getUuid())) deaths.unghost(player);
                KuudraItems.strip(player);
            }
            if (bossState.health() > 0) bossState.sink();
        }
    }

    public void dispose() {
        synchronized (run) {
            stop();
            if (task != null) task.cancel();
            if (boss != null) boss.remove();
            if (ballista != null) ballista.remove();
            if (stunCycle != null) stunCycle.dispose();
            artillery.cannons.forEach(KuudraCannon::remove);
            props.forEach(Displays::remove);
        }
    }

    public void join(SkyBlockPlayer player) {
        tokens.join(player.getUuid(), (int) EssencePerks.value(player, CrimsonEssencePerk.HEADSTART));
        perks.of(player.getUuid());
    }

    public void leave(SkyBlockPlayer player) {
        synchronized (run) {
            release(player);
            if (stunCycle != null) stunCycle.leave(player);
            KuudraItems.strip(player);
        }
    }

    /** Lets go of whatever the player holds or rides: carried cargo, a cannon, the Ballista. */
    void release(SkyBlockPlayer player) {
        supplies.dropCarried(player.getUuid(), true);
        artillery.knockOffCannon(player);
        if (ballista != null && ballista.isRiding(player)) ballista.dismount();
        supplies.cargo.forEach(item -> {
            if (item.pickup() != null && item.pickup().player().equals(player.getUuid())) item.setPickup(null);
        });
    }

    private void tickSafely() {
        synchronized (run) {
            if (stopped && boss == null) return;
            try {
                tick();
            } catch (Exception exception) {
                Logger.error(exception, "Kuudra fight tick failed in run {}", run.id());
            }
        }
    }

    private void tick() {
        tick++;
        List<SkyBlockPlayer> online = run.onlineMembers();
        if (boss != null) {
            KuudraBossState.Event event = bossState.tick();
            if (event == KuudraBossState.Event.SUBMERGED) {
                boss.moveSide(ThreadLocalRandom.current());
                if (stunCycle != null && !stopped) stunCycle.onSubmerged();
            }
            if (event == KuudraBossState.Event.SURFACED) {
                instance.playSound(Sound.sound(Key.key("entity.wither.ambient"), Sound.Source.HOSTILE, 5f, 0.49f),
                        boss.base().x(), boss.base().y(), boss.base().z());
                if (stunCycle != null && !stopped) stunCycle.onSurfaced();
            }
            if (event == KuudraBossState.Event.RECOVERED && stunCycle != null && !stopped) stunCycle.onRecovered();
            boss.tick(online);
        }
        if (stopped) return;
        if (deathFinale != null) {
            for (KuudraFinale.Step step : deathFinale.tick()) {
                if (step.kind() == KuudraFinale.Kind.FINISH) {
                    if (boss != null) instance.playSound(Sound.sound(Key.key("entity.skeleton_horse.death"), Sound.Source.HOSTILE,
                            6f, 0.59f), boss.base().x(), boss.base().y(), boss.base().z());
                    run.finish(dev.daweapon.type.kuudra.run.KuudraRunState.Outcome.WIN);
                    return;
                }
                List<String> lines = dialogue.chat("defeated", ThreadLocalRandom.current());
                if (step.index() < lines.size()) run.broadcast(lines.get(step.index()));
            }
        }
        if (stunCycle != null) stunCycle.tick(tick);
        if (lair != null) lair.tick(tick);
        if (rod != null) rod.tick(player -> KuudraItems.kindOf(player.getItemInMainHand()) == KuudraItems.Kind.ELLES_LAVA_ROD);
        supplies.cargo.forEach(LavaCargo::tick);
        supplies.tickPickups();
        supplies.tickCarries();
        ballistaOps.tickBuild();
        artillery.tickShots();
        combat.tickChunks();
        minions.tickLeaps();
        if (tick % 20 == 0) minions.rescueFallenMobs();
        if (ballista != null) ballista.tick();
        artillery.cannons.forEach(KuudraCannon::tick);
        if (slams != null) slams.tick(tentacleOps::landSlam);
        tentacleOps.tickTentacleRespawns();
        lives.tick(tick).forEach(uuid -> online.stream().filter(p -> p.getUuid().equals(uuid)).findFirst().ifPresent(deaths::respawn));
        if (tick % 20 == 0) display.tickAuras();
        if (tick % 20 == 0) deaths.tickReviveTitles(online);
        if (tick % 5 == 0) display.tickActionBars(online);
        int limit = spec.lives().timeLimitSeconds();
        if (limit > 0 && run.state().elapsedSeconds() >= limit) run.finish(dev.daweapon.type.kuudra.run.KuudraRunState.Outcome.LOSE);
    }

    void later(int ticks, Runnable action) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            synchronized (run) {
                if (!stopped) action.run();
            }
        }).delay(TaskSchedule.tick(Math.max(1, ticks))).schedule();
    }

    private void recordHighestWave() {
        int wave = run.state().wave();
        for (SkyBlockPlayer player : run.onlineMembers()) {
            try {
                KuudraProgress.recordWave(player, tier, wave);
            } catch (Exception exception) {
                Logger.error(exception, "Couldn't record {}'s highest Kuudra wave", player.getUsername());
            }
        }
    }

    public void openPerkShop(SkyBlockPlayer player) {
        player.openView(new GUIKuudraPerkShop(this));
    }

    // ---- delegates ------------------------------------------------------------------------------------------

    public boolean reel(SkyBlockPlayer player, Pos bobber) { return supplies.reel(player, bobber); }

    public boolean isCarrying(SkyBlockPlayer player) { return supplies.isCarrying(player); }

    public boolean locksHands(SkyBlockPlayer player) { return supplies.carrying.containsKey(player.getUuid()); }

    int missingFuel() { return supplies.missingFuel(); }

    double buildSpeed(UUID uuid) { return ballistaOps.buildSpeed(uuid); }

    double ballistaDamage(SkyBlockPlayer player) { return ballistaOps.ballistaDamage(player); }

    double cannonDamage(SkyBlockPlayer player, KuudraMob target) { return artillery.cannonDamage(player, target); }

    public void knockOffCannon(SkyBlockPlayer player) { artillery.knockOffCannon(player); }

    public void damageKuudra(double percent) { combat.damageKuudra(percent); }

    void damageKuudraPoints(double points) { combat.damageKuudraPoints(points); }

    void closeStomach() { combat.closeStomach(); }

    public boolean arrowHitChunk(SkyBlockPlayer player, Entity target) { return combat.arrowHitChunk(player, target); }

    public void arrowHitKuudra(SkyBlockPlayer player, double damage) { combat.arrowHitKuudra(player, damage); }

    public boolean swing(SkyBlockPlayer player) { return input.swing(player); }

    public void attack(SkyBlockPlayer player, Entity target) { input.attack(player, target); }

    public void sneak(SkyBlockPlayer player) { input.sneak(player); }

    public boolean useItem(SkyBlockPlayer player, ItemStack held) { return input.useItem(player, held); }

    public void damaged(SkyBlockPlayer player, EntityDamageEvent event) { input.damaged(player, event); }

    public boolean death(SkyBlockPlayer player) { return deaths.death(player); }

    void killInLair(SkyBlockPlayer player) { deaths.death(player, true); }

    public boolean revive(SkyBlockPlayer reviver, boolean finalKilled) { return deaths.revive(reviver, finalKilled); }

    public boolean hasGhost(boolean finalKilled) { return deaths.hasGhost(finalKilled); }

    public void spawnWave(int number) { minions.spawnWave(number); }

    public @Nullable KuudraMob spawnMob(String mobId, Pos at) { return minions.spawnMob(mobId, at); }

    public void slam(KuudraTentacle tentacle) { tentacleOps.slam(tentacle); }

    void flailSlam(Pos from, double damage) { tentacleOps.flailSlam(from, damage); }

    public void forget(KuudraMob mob) { minions.forget(mob); }

    public void onMobKilled(KuudraMob mob, SkyBlockPlayer killer) { minions.onMobKilled(mob, killer); }

    public void onMobHit(SkyBlockPlayer player, KuudraMob mob, double dealt) { minions.onMobHit(player, mob, dealt); }

    public double outgoingMultiplier(SkyBlockPlayer player, KuudraMob mob) { return minions.outgoingMultiplier(player, mob); }

    public void mergeMagmaFollowers(List<KuudraSpecialists.MagmaFollower> group, Pos at) { minions.mergeMagmaFollowers(group, at); }

    public void explode(Pos at, double radius, double damage, KuudraMob source) { minions.explode(at, radius, damage, source); }

    public void primeTnt(Pos at, int fuseTicks, double damage, KuudraMob source) { minions.primeTnt(at, fuseTicks, damage, source); }

    public void dropshipBomb(KuudraMob ship, double damage) { minions.dropshipBomb(ship, damage); }

    public Hud hud(SkyBlockPlayer player) { return display.hud(player); }

    public List<String> summary(SkyBlockPlayer player, boolean win) { return display.summary(player, win); }

    public Pos centre() { return stage.centre(); }

    public double loopRadius() { return stage.loopRadius(); }

    public Pos randomPlatformPoint(double inset) { return stage.randomPlatformPoint(inset); }

    boolean onMainPlatform(Pos at) { return stage.onMainPlatform(at); }

    Pos landingFor(Pos from) { return stage.landingFor(from); }

    public void ring(Pos at, double radius, Particle particle) { stage.ring(at, radius, particle); }

    void playAt(Pos at, String sound, float pitch) { stage.playAt(at, sound, pitch); }

    void playAll(String sound, float pitch) { stage.playAll(sound, pitch); }

    Pos randomEdge() { return stage.edgePoint(); }

    public void completeSupplies() { staff.completeSupplies(); }

    public void completeBuild() { staff.completeBuild(); }

    public void completeFuel() { staff.completeFuel(); }

    public StaffResult eat(SkyBlockPlayer player) { return staff.eat(player); }

    public StaffResult stun(SkyBlockPlayer player) { return staff.stun(player); }

    public StaffResult spit(SkyBlockPlayer player) { return staff.spit(player); }

    public StaffResult lair() { return staff.lair(); }

    public void giveTokens(int amount) {
        synchronized (run) {
            tokens.gain(amount);
        }
    }

    // ---- state and accessors ---------------------------------------------------------------------------------

    @Nullable SkyBlockPlayer member(UUID uuid) {
        for (SkyBlockPlayer player : run.onlineMembers()) {
            if (player.getUuid().equals(uuid)) return player;
        }
        return null;
    }

    public List<SkyBlockPlayer> alivePlayers() {
        return run.onlineMembers().stream().filter(player -> !lives.isGhost(player.getUuid())).toList();
    }

    public boolean isTargetable(SkyBlockPlayer player) {
        return !stopped && run.isMember(player.getUuid()) && player.getInstance() == instance
                && !lives.isGhost(player.getUuid()) && player.getGameMode() != GameMode.SPECTATOR
                && player.getGameMode() != GameMode.CREATIVE;
    }

    public KuudraTier tier() {
        return tier;
    }

    public KuudraFightSpec spec() {
        return spec;
    }

    public KuudraTierSpec tierSpec() {
        return tierSpec;
    }

    public KuudraPerks perks() {
        return perks;
    }

    public KuudraTokens tokens() {
        return tokens;
    }

    public KuudraPerkShop shop() {
        return config.perkShop();
    }

    public KuudraRun run() {
        return run;
    }

    public KuudraBossState bossState() {
        return bossState;
    }

    public int livingMobs() {
        return mobs.size();
    }

    public boolean isGhost(SkyBlockPlayer player) {
        return lives.isGhost(player.getUuid());
    }

    public boolean isKuudra(Entity entity) {
        return boss != null && boss.is(entity);
    }

    public boolean isFinalGhost(SkyBlockPlayer player) {
        return lives.isFinal(player.getUuid());
    }

    public boolean isInStomach(SkyBlockPlayer player) {
        return stunCycle != null && stunCycle.isInside(player);
    }

    public List<SkyBlockPlayer> stomachPlayers() {
        if (stunCycle == null) return List.of();
        Set<UUID> inside = stunCycle.inside();
        return alivePlayers().stream().filter(player -> inside.contains(player.getUuid())).toList();
    }

    List<KuudraTentacle> tentacles() {
        return tentacleOps.tentacles();
    }

    /** The platform centre falls into the lair, and the Ballista with it. */
    void removeBallista() {
        if (ballista == null) return;
        ballista.dismount();
        ballista.remove();
        ballista = null;
    }

    Instance instance() {
        return instance;
    }

    public KuudraArena arena() {
        return arena;
    }

    KuudraDialogue dialogue() {
        return dialogue;
    }

    @Nullable KuudraBoss boss() {
        return boss;
    }

    long currentTick() {
        return tick;
    }

    boolean armed() {
        return fuel.armed();
    }

    /** Kuudra is dying or the Infernal skip has begun: no more stun cycles. */
    boolean finishing() {
        return finishing || (lair != null && lair.started());
    }
}
