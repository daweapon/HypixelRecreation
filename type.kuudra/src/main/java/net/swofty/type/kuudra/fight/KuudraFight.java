package net.swofty.type.kuudra.fight;

import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.entity.ArmorStandModels;
import net.swofty.type.kuudra.fight.world.BallistaSite;
import net.swofty.type.kuudra.fight.world.Displays;
import net.swofty.type.kuudra.fight.world.ElleLavaRod;
import net.swofty.type.kuudra.fight.world.KuudraBoss;
import net.swofty.type.kuudra.fight.world.KuudraCannon;
import net.swofty.type.kuudra.fight.world.KuudraItems;
import net.swofty.type.kuudra.fight.world.LavaCargo;
import net.swofty.type.kuudra.fight.world.Projectile;
import net.swofty.type.kuudra.fight.world.TentacleSlams;
import net.swofty.type.kuudra.gui.GUIKuudraPerkShop;
import net.swofty.type.kuudra.mob.KuudraMob;
import net.swofty.type.kuudra.mob.KuudraMobs;
import net.swofty.type.kuudra.mob.KuudraRushers;
import net.swofty.type.kuudra.mob.KuudraSpecialists;
import net.swofty.type.kuudra.mob.KuudraTentacle;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.skyblockgeneric.accessory.handler.UsableAccessory;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.essence.CrimsonEssencePerk;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;

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

    private record FlailSource(@Nullable KuudraTentacle tentacle, double damage) {
    }

    private record Carried(LavaCargo.Kind kind, String spot, Pos home, CargoCarry carry, ItemStack replaced) {
    }

    private enum ShotKind {BOLT, CANNONBALL}

    private record Shot(Projectile projectile, ShotKind kind) {
    }

    private record Leap(KuudraMob mob, Pos from, Pos to, long start) {
    }

    private static final int LEAP_TICKS = 16;
    private static final Set<String> FLYERS = Set.of("WANDERING_BLAZE", "DROPSHIP");
    private static final int REVIVE_TITLE_SECONDS = 5;
    private static final int LAIR_DEATH_BLASTS = 7;


    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraConfig config;
    private final KuudraFightSpec spec;
    private final KuudraTierSpec tierSpec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final Instance instance;
    private final KuudraTokens tokens;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final KuudraBossState bossState;
    private final BallistaFuel fuel;

    private final List<LavaCargo> cargo = new CopyOnWriteArrayList<>();
    private final Map<UUID, Carried> carrying = new ConcurrentHashMap<>();
    private final Set<String> filledPiles = ConcurrentHashMap.newKeySet();
    private final Set<KuudraMob> mobs = ConcurrentHashMap.newKeySet();
    private final List<Long> tentacleRespawns = new CopyOnWriteArrayList<>();
    private final List<KuudraCannon> cannons = new ArrayList<>();
    private final List<Shot> shots = new CopyOnWriteArrayList<>();
    private final List<Leap> leaps = new CopyOnWriteArrayList<>();
    private final List<Entity> props = new ArrayList<>();
    private static final int MOUNT_GRACE_TICKS = 10;
    private final Map<UUID, Long> shotReady = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cannonReady = new ConcurrentHashMap<>();
    private final Map<UUID, Long> futileReady = new ConcurrentHashMap<>();
    private final Map<UUID, Long> freshToolsUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> freshToolsRolled = new ConcurrentHashMap<>();

    private @Nullable BallistaBuild build;
    private @Nullable BallistaSite ballista;
    private @Nullable KuudraBoss boss;
    private @Nullable TentacleSlams slams;
    private @Nullable ElleLavaRod rod;
    private @Nullable Task task;
    private @Nullable KuudraStunCycle stunCycle;
    private @Nullable KuudraLairPhase lair;
    private @Nullable KuudraFinale deathFinale;
    private boolean finishing;
    private int suppliesPlaced;
    private boolean emerged;
    private boolean stopped;
    private long tick;
    private long lastBallistaBark = Long.MIN_VALUE;

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
    }

    // ---- lifecycle -------------------------------------------------------------------------------------------

    public void start() {
        synchronized (run) {
            run.onlineMembers().forEach(this::join);
            ballista = new BallistaSite(instance, arena, this::usePile, this::useBallistaSeat);
            arena.cannons().forEach(at -> cannons.add(new KuudraCannon(instance, at, this::useCannon)));
            for (ArmorStandModels.Stand stand : ArmorStandModels.load("/kuudra_models.json", "perk_shop")) {
                LivingEntity entity = stand.create();
                entity.setInstance(instance, stand.position());
                props.add(entity);
            }
            props.add(Displays.text(instance, arena.perkShop().add(0, 1.9, 0), "<a><l>Perk Shop"));
            props.add(Displays.hitbox(instance, arena.perkShop(), 1.2f, 1.2f, this::openPerkShop));
            slams = new TentacleSlams(instance, arena.floorY() - 1, spec.tentacles().telegraphTicks());
            rod = new ElleLavaRod(instance, arena.lavaSurfaceY(), this::reelElleRod);
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
            for (int i = 0; i < spec.tentacles().active(tier); i++) spawnTentacle();
            if (run.state().phase() == KuudraPhase.SUPPLIES) spawnCargo(LavaCargo.Kind.SUPPLIES, spec.supplies().crates());
            spawnWave(run.state().wave());
        }
    }

    public void onPhase(@Nullable KuudraPhase phase) {
        if (phase == null) return;
        synchronized (run) {
            if (stopped) return;
            if (!emerged) emerge();
            switch (phase) {
                case SUPPLIES -> {
                    if (cargo.stream().noneMatch(each -> each.kind() == LavaCargo.Kind.SUPPLIES)) {
                        spawnCargo(LavaCargo.Kind.SUPPLIES, spec.supplies().crates() - suppliesPlaced);
                    }
                }
                case BUILD -> {
                    clearCargo();
                    arena.piles().keySet().forEach(pile -> {
                        filledPiles.add(pile);
                        if (ballista != null) ballista.supplyReceived(pile);
                    });
                    suppliesPlaced = Math.max(suppliesPlaced, spec.supplies().crates());
                    build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                    bossState.setCycling(false);
                    bossState.sink();
                    later(160, () -> dialogue.chat("submerge_after_supplies", ThreadLocalRandom.current()).forEach(run::broadcast));
                }
                case FUEL -> {
                    clearCargo();
                    if (build == null) build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                    build.completeAll();
                    if (ballista != null) ballista.showReady(fuel.loaded(), fuel.cells());
                    spawnCargo(LavaCargo.Kind.FUEL, missingFuel());
                    bossState.setCycling(true);
                    bossState.rise();
                    if (stunCycle != null) stunCycle.begin();
                }
                case DPS, STUN -> {
                    boolean stunCycleStep = stunCycle != null && stunCycle.begun();
                    if (!stunCycleStep || phase == KuudraPhase.STUN) {
                        clearCargo();
                        if (build == null) build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                        build.completeAll();
                        fuel.fill();
                        if (ballista != null) ballista.showReady(fuel.loaded(), fuel.cells());
                        bossState.setCycling(true);
                        bossState.rise();
                    }
                    if (stunCycle != null) {
                        stunCycle.begin();
                        if (phase == KuudraPhase.STUN) stunCycle.charged();
                        if (phase == KuudraPhase.DPS && !bossState.isStunned() && !stunCycle.stunNow()) {
                            run.broadcast("<c>Kuudra must be up to be stunned; he will be stunned when a pod breaks.");
                        }
                    }
                }
                case LAIR -> {
                    if (lair != null) lair.begin();
                }
            }
        }
    }

    /** The run is over: the fight stops, minions vanish and the run-only items are taken back. */
    public void stop() {
        synchronized (run) {
            if (stopped) return;
            stopped = true;
            recordHighestWave();
            clearCargo();
            new ArrayList<>(carrying.keySet()).forEach(uuid -> dropCarried(uuid, false));
            new ArrayList<>(mobs).forEach(KuudraMob::remove);
            mobs.clear();
            shots.forEach(shot -> shot.projectile().remove());
            shots.clear();
            if (slams != null) slams.clear();
            if (stunCycle != null) stunCycle.close();
            if (lair != null) lair.stop();
            if (rod != null) rod.clear();
            cannons.forEach(KuudraCannon::dismount);
            if (ballista != null) ballista.dismount();
            for (SkyBlockPlayer player : run.onlineMembers()) {
                if (lives.isGhost(player.getUuid())) unghost(player);
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
            cannons.forEach(KuudraCannon::remove);
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
        dropCarried(player.getUuid(), true);
        knockOffCannon(player);
        if (ballista != null && ballista.isRiding(player)) ballista.dismount();
        cargo.forEach(item -> {
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
                    run.finish(net.swofty.type.kuudra.run.KuudraRunState.Outcome.WIN);
                    return;
                }
                List<String> lines = dialogue.chat("defeated", ThreadLocalRandom.current());
                if (step.index() < lines.size()) run.broadcast(lines.get(step.index()));
            }
        }
        if (stunCycle != null) stunCycle.tick(tick);
        if (lair != null) lair.tick(tick);
        if (rod != null) rod.tick(player -> KuudraItems.kindOf(player.getItemInMainHand()) == KuudraItems.Kind.ELLES_LAVA_ROD);
        cargo.forEach(LavaCargo::tick);
        tickPickups();
        tickCarries();
        tickBuild();
        tickShots();
        tickLeaps();
        if (tick % 20 == 0) rescueFallenMobs();
        if (ballista != null) ballista.tick();
        cannons.forEach(KuudraCannon::tick);
        if (slams != null) slams.tick(this::landSlam);
        tickTentacleRespawns();
        lives.tick(tick).forEach(uuid -> online.stream().filter(p -> p.getUuid().equals(uuid)).findFirst().ifPresent(this::respawn));
        if (tick % 20 == 0) tickAuras();
        if (tick % 20 == 0) tickReviveTitles(online);
        if (tick % 5 == 0) tickActionBars(online);
        int limit = spec.lives().timeLimitSeconds();
        if (limit > 0 && run.state().elapsedSeconds() >= limit) run.finish(net.swofty.type.kuudra.run.KuudraRunState.Outcome.LOSE);
    }

    void later(int ticks, Runnable action) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            synchronized (run) {
                if (!stopped) action.run();
            }
        }).delay(TaskSchedule.tick(Math.max(1, ticks))).schedule();
    }

    // ---- supplies and fuel -----------------------------------------------------------------------------------

    private void spawnCargo(LavaCargo.Kind kind, int count) {
        if (count <= 0) return;
        var supplies = spec.supplies();
        List<String> spots = SupplySpots.pick(arena.supplySpots().keySet(), Math.min(count, arena.supplySpots().size()),
                ThreadLocalRandom.current());
        for (String spot : spots) {
            Pos home = SupplySpots.crateFor(arena, arena.supplySpots().get(spot), supplies.crateDistance(),
                    supplies.crateJitter(), ThreadLocalRandom.current());
            cargo.add(new LavaCargo(instance, kind, spot, home, this::useCargo));
        }
        run.onlineMembers().forEach(player -> player.playSound(
                Sound.sound(Key.key("entity.ender_dragon.growl"), Sound.Source.HOSTILE, 3f, 0.49f)));
    }

    private boolean floats(Pos position) {
        return instance.getBlock(position.blockX(), arena.lavaSurfaceY() - 1, position.blockZ()).compare(Block.LAVA);
    }

    private Pos shoreOf(LavaCargo item) {
        return arena.supplySpots().get(item.spot()).position();
    }

    private void clearCargo() {
        cargo.forEach(LavaCargo::remove);
        cargo.clear();
    }

    private void reelElleRod(SkyBlockPlayer player, Pos bobber) {
        synchronized (run) {
            reel(player, bobber);
        }
    }

    /** A Lava Rod reeled in next to a crate or Fuel Cell pulls it toward the player (Strongarm pulls further). */
    public boolean reel(SkyBlockPlayer player, Pos bobber) {
        synchronized (run) {
            if (stopped || lives.isGhost(player.getUuid())) return false;
            LavaCargo nearest = null;
            double best = spec.supplies().hookRadius();
            for (LavaCargo each : cargo) {
                double distance = Math.hypot(each.position().x() - bobber.x(), each.position().z() - bobber.z());
                if (distance <= best && each.pickup() == null) {
                    best = distance;
                    nearest = each;
                }
            }
            if (nearest == null) return false;
            double pull = spec.supplies().reelPullBlocks() * (1 + EssencePerks.value(player, CrimsonEssencePerk.STRONGARM) / 100);
            Pos moved = SupplySpots.reel(nearest.position(), player.getPosition(), pull, this::floats);
            if (moved.distance(nearest.position()) < 0.25) {
                player.sendMessage("<c>The supplies can't move any further in your direction!");
            }
            nearest.moveTo(moved);
            return true;
        }
    }

    private void useCargo(LavaCargo item, SkyBlockPlayer player) {
        synchronized (run) {
            if (stopped || !cargo.contains(item) || lives.isGhost(player.getUuid()) || carrying.containsKey(player.getUuid())) return;
            CargoPickup current = item.pickup();
            if (current != null) {
                if (!current.player().equals(player.getUuid())) player.sendMessage(dialogue.announcement("supplies_taken", Map.of()));
                return;
            }
            if (cargo.stream().anyMatch(other -> other.pickup() != null && other.pickup().player().equals(player.getUuid()))) return;
            if (player.getPosition().distance(item.position()) > spec.supplies().pickupRange()) {
                player.sendMessage("<c>It's too far away! Pull it closer with a Lava Rod.");
                return;
            }
            double bonus = perks.fraction(player.getUuid(), "STEADY_HANDS") + organ(player, spec.supplies().organBonus());
            int ticks = CargoPickup.ticksNeeded(spec.supplies().pickupSeconds().get(tier), bonus);
            item.setPickup(new CargoPickup(player.getUuid(), player.getPosition().x(), player.getPosition().z(),
                    spec.supplies().moveTolerance(), ticks));
            player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) spec.supplies().pickupSlownessAmplifier(), ticks + 10));
            player.playSound(Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.PLAYER, 2f, 2f));
        }
    }

    private void tickPickups() {
        for (LavaCargo item : cargo) {
            if (tick % 5 == 0) item.setReachable(SupplySpots.atShore(item.position(), shoreOf(item), this::floats));
            CargoPickup pickup = item.pickup();
            if (pickup == null) continue;
            SkyBlockPlayer player = member(pickup.player());
            if (player == null || lives.isGhost(player.getUuid())) {
                item.setPickup(null);
                continue;
            }
            switch (pickup.tick(player.getPosition().x(), player.getPosition().z())) {
                case PROGRESS -> {
                    if (tick % 5 == 0) {
                        player.showTitle(Text.parse(pickup.bar()), Text.parse("<c>Don't Move!"),
                                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO));
                    }
                }
                case SLIPPED -> {
                    item.setPickup(null);
                    player.removeEffect(PotionEffect.SLOWNESS);
                    player.sendMessage(dialogue.announcement("supplies_slipped", Map.of()));
                }
                case DONE -> {
                    cargo.remove(item);
                    item.remove();
                    player.removeEffect(PotionEffect.SLOWNESS);
                    player.showTitle(Text.parse(pickup.bar()), Text.parse("<c>Don't Move!"),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO));
                    playAt(item.position(), "block.chest.close", 0.49f);
                    carry(player, item.kind(), item.spot(), item.home());
                }
            }
        }
    }

    /** The crate goes into the last hotbar slot and stays selected (the Burning capture: "Elle's Supplies"). */
    private void carry(SkyBlockPlayer player, LavaCargo.Kind kind, String spot, Pos home) {
        ItemStack replaced = player.getInventory().getItemStack(KuudraItems.CARRY_SLOT);
        player.getInventory().setItemStack(KuudraItems.CARRY_SLOT, KuudraItems.create(kind == LavaCargo.Kind.SUPPLIES
                ? KuudraItems.Kind.ELLES_SUPPLIES : KuudraItems.Kind.FUEL_CELL));
        player.setHeldItemSlot((byte) KuudraItems.CARRY_SLOT);
        carrying.put(player.getUuid(), new Carried(kind, spot, home,
                new CargoCarry(player.getUuid(), spec.supplies().carryTimeoutSeconds()), replaced));
        player.sendMessage(kind == LavaCargo.Kind.SUPPLIES ? dialogue.announcement("supplies_retrieved", Map.of())
                : "<a>You retrieved a Ballista Fuel Cell from the Lava!");
        player.playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.PLAYER, 1f, 0.8f));
    }

    private void tickCarries() {
        for (Map.Entry<UUID, Carried> entry : carrying.entrySet()) {
            SkyBlockPlayer player = member(entry.getKey());
            Carried carried = entry.getValue();
            if (player == null) {
                dropCarried(entry.getKey(), false);
                continue;
            }
            if (carried.carry().tick()) {
                dropCarried(player.getUuid(), true);
                continue;
            }
            actionBar(player, "<e>Carrying " + carried.kind().itemName() + " <7>(" + carried.carry().secondsLeft() + "s)");
        }
    }

    /** Right-clicking a pile while holding Elle's Supplies drops them off (the Burning capture). */
    private void usePile(String pile, SkyBlockPlayer player) {
        synchronized (run) {
            Carried carried = carrying.get(player.getUuid());
            if (stopped || carried == null || carried.kind() != LavaCargo.Kind.SUPPLIES) return;
            if (run.state().phase() != KuudraPhase.SUPPLIES) return;
            if (filledPiles.contains(pile)) {
                player.sendMessage("<c>This pile already has its supplies!");
                return;
            }
            if (horizontal(player.getPosition(), arena.piles().get(pile)) > spec.supplies().placeRange()) return;
            finishCarry(player.getUuid());
            filledPiles.add(pile);
            suppliesPlaced++;
            if (ballista != null) {
                ballista.supplyReceived(pile);
                ballista.showCollected(suppliesPlaced, spec.supplies().crates());
            }
            playAt(arena.piles().get(pile), "block.ladder.break", 1.5f);
            run.broadcast(dialogue.announcement("supply_recovered",
                    Map.of("player", player.getFullDisplayName().toString(), "count", suppliesPlaced)));
            playAll("block.note_block.pling", 2f);
            tokens.gain(spec.tokens().supplyRecovered());
            if (suppliesPlaced >= spec.supplies().crates()) run.advancePhase();
        }
    }

    private boolean tryLoadFuel(SkyBlockPlayer player) {
        Carried carried = carrying.get(player.getUuid());
        if (carried == null || carried.kind() != LavaCargo.Kind.FUEL) return false;
        if (ballista == null || fuel.armed() || !fuelPhase(run.state().phase())) return true;
        if (horizontal(player.getPosition(), ballista.base()) > spec.fuel().loadRange()) {
            player.sendMessage("<c>Get closer to the Ballista to load the Fuel Cell!");
            return true;
        }
        finishCarry(player.getUuid());
        BallistaFuel.Load load = fuel.load();
        run.broadcast(player.getFullDisplayName() + " <a>loaded a Ballista Fuel Cell! <7>(" + fuel.loaded() + "/" + fuel.cells() + ")");
        playAll("block.anvil.use", 1.2f);
        tokens.gain(spec.tokens().fuelLoaded());
        ballista.showReady(fuel.loaded(), fuel.cells());
        boolean fuelling = run.state().phase() == KuudraPhase.FUEL;
        if (load == BallistaFuel.Load.FIRST_FULL && fuelling) run.advancePhase();
        if (load == BallistaFuel.Load.REFUELLED) {
            run.broadcast("<a>The Ballista is fuelled! <e>Mount it and fire at Kuudra!");
            if (fuelling) run.resumePhase(tierSpec.stunRequired() ? KuudraPhase.STUN : KuudraPhase.DPS);
        }
        return true;
    }

    private static boolean fuelPhase(@Nullable KuudraPhase phase) {
        return phase == KuudraPhase.FUEL || phase == KuudraPhase.STUN || phase == KuudraPhase.DPS;
    }

    /** Fuel Cells still needed for a full load, after those loaded, floating in the lava and being carried. */
    int missingFuel() {
        long floating = cargo.stream().filter(item -> item.kind() == LavaCargo.Kind.FUEL).count();
        long carried = carrying.values().stream().filter(each -> each.kind() == LavaCargo.Kind.FUEL).count();
        return fuel.missing(floating, carried);
    }

    /** One shot empties the Ballista: everyone fishes up a fresh load of Fuel Cells before it fires again. */
    private void spendFuel() {
        if (!fuel.fire()) return;
        if (ballista != null) {
            ballista.dismount();
            ballista.showReady(0, spec.fuel().cells());
        }
        run.broadcast("<e>The Ballista is out of fuel! <c>Fish up " + spec.fuel().cells() + " more Fuel Cells <e>to fire it again.");
        KuudraPhase phase = run.state().phase();
        if (phase == KuudraPhase.STUN || (phase == KuudraPhase.DPS && !tierSpec.stunRequired())) {
            run.resumePhase(KuudraPhase.FUEL);
        }
        spawnCargo(LavaCargo.Kind.FUEL, missingFuel());
    }

    private void finishCarry(UUID player) {
        Carried carried = carrying.remove(player);
        if (carried == null) return;
        SkyBlockPlayer member = member(player);
        if (member == null) return;
        if (KuudraItems.kindOf(member.getInventory().getItemStack(KuudraItems.CARRY_SLOT)) != null) {
            member.getInventory().setItemStack(KuudraItems.CARRY_SLOT, carried.replaced());
        }
    }

    /** The carried crate or Fuel Cell falls back into the lava at its spot. */
    private void dropCarried(UUID player, boolean announce) {
        Carried carried = carrying.get(player);
        if (carried == null) return;
        finishCarry(player);
        if (!stopped) cargo.add(new LavaCargo(instance, carried.kind(), carried.spot(), carried.home(), this::useCargo));
        SkyBlockPlayer member = member(player);
        if (announce && member != null) {
            run.broadcast(dialogue.announcement("supplies_dropped", Map.of("player", member.getFullDisplayName().toString())));
        }
    }

    public boolean isCarrying(SkyBlockPlayer player) {
        return carrying.containsKey(player.getUuid());
    }

    // ---- Ballista --------------------------------------------------------------------------------------------

    /** Sneak + left-click on a pile builds it (research K1 3.2). */
    private void hitPile(String pile, SkyBlockPlayer player) {
        if (build == null || run.state().phase() != KuudraPhase.BUILD || !player.isSneaking()) return;
        if (lives.isGhost(player.getUuid()) || carrying.containsKey(player.getUuid())) return;
        if (horizontal(player.getPosition(), arena.piles().get(pile)) > spec.build().buildRange()) return;
        if (!build.click(player.getUuid(), pile, tick, spec.build().clickHoldTicks())) return;
        playAt(arena.piles().get(pile), "block.ladder.break", 1f);
        long rollEvery = spec.build().freshToolsRollSeconds() * 20L;
        if (tick - freshToolsRolled.getOrDefault(player.getUuid(), Long.MIN_VALUE / 2) >= rollEvery) {
            freshToolsRolled.put(player.getUuid(), tick);
            double chance = EssencePerks.value(player, CrimsonEssencePerk.FRESH_TOOLS) / 100;
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance
                    && freshToolsUntil.getOrDefault(player.getUuid(), 0L) < tick) {
                freshToolsUntil.put(player.getUuid(), tick + spec.build().freshToolsSeconds() * 20L);
                player.sendMessage(dialogue.announcement("fresh_tools", Map.of()));
            }
        }
    }

    double buildSpeed(UUID uuid) {
        SkyBlockPlayer player = member(uuid);
        double organs = player == null ? 0 : organ(player, spec.build().organBonus());
        double fresh = freshToolsUntil.getOrDefault(uuid, 0L) >= tick ? 2 : 1;
        return (1 + perks.fraction(uuid, "BALLISTA_MECHANIC") + organs) * fresh;
    }

    private void tickBuild() {
        if (build == null || ballista == null || run.state().phase() != KuudraPhase.BUILD) return;
        List<String> finished = build.tick(tick, this::buildSpeed);
        finished.forEach(pile -> playAt(arena.piles().get(pile), "block.anvil.use", 1.5f));
        if (tick % 10 == 0 && build.helping() > 0) playAt(ballista.base(), "block.anvil.land", 1.7f);
        if (tick % (spec.build().idleDecaySeconds() * 20L) == 0) build.decayIdle(spec.build().idleDecayProgress());
        if (tick % (spec.build().mobHitIntervalSeconds() * 20L) == 0) {
            long attackers = mobs.stream().filter(KuudraMob::attacksBallista)
                    .filter(mob -> !mob.isDead() && mob.getPosition().distance(ballista.base()) <= spec.build().mobHitRange())
                    .count();
            boolean damaged = false;
            for (int i = 0; i < attackers; i++) {
                damaged |= build.knockAny(spec.build().mobHitProgress(), ThreadLocalRandom.current()) != null;
            }
            if (damaged) barkBallistaDamaged();
        }
        if (tick % 5 == 0) ballista.showBuild(build);
        if (build.complete()) {
            ballista.showReady(fuel.loaded(), fuel.cells());
            run.advancePhase();
        }
    }

    private void barkBallistaDamaged() {
        long cooldown = spec.build().damageBarkCooldownSeconds() * 20L;
        if (tick - lastBallistaBark < cooldown) return;
        lastBallistaBark = tick;
        dialogue.chat("ballista_damaged", ThreadLocalRandom.current()).forEach(run::broadcast);
    }

    private void useBallistaSeat(SkyBlockPlayer player) {
        synchronized (run) {
            if (ballista == null || stopped || lives.isGhost(player.getUuid()) || ballista.isRiding(player)) return;
            if (tryLoadFuel(player) || carrying.containsKey(player.getUuid())) return;
            KuudraPhase phase = run.state().phase();
            if (phase == KuudraPhase.SUPPLIES || phase == KuudraPhase.BUILD) {
                player.sendMessage("<c>The Ballista isn't built yet!");
                return;
            }
            if (!fuel.armed()) {
                player.sendMessage("<c>The Ballista needs " + fuel.cells() + " Fuel Cells to fire! <7>(" + fuel.loaded() + "/" + fuel.cells() + ")");
                return;
            }
            if (!ballista.mount(player)) {
                player.sendMessage("<c>Someone is already using the Ballista!");
                return;
            }
            shotReady.put(player.getUuid(), tick + MOUNT_GRACE_TICKS);
            player.sendMessage("<a>You mounted the Ballista! <e>Look to aim, left-click <a>to fire, sneak to get off.");
        }
    }

    private void fireBallista(SkyBlockPlayer player) {
        if (ballista == null || boss == null) return;
        long ready = shotReady.getOrDefault(player.getUuid(), 0L);
        if (tick < ready) return;
        shotReady.put(player.getUuid(), tick + spec.fuel().shotCooldownTicks());
        if (!fuel.armed()) {
            player.sendMessage("<c>The Ballista is out of fuel!");
            return;
        }
        if (!bossState.canBeShot()) {
            player.sendMessage("<c>Kuudra is out of reach!");
            return;
        }
        Pos muzzle = ballista.muzzle();
        Vec look = player.getPosition().direction().normalize();
        Vec toKuudra = boss.mouth().sub(muzzle).asVec().normalize();
        boolean onTarget = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.dot(toKuudra))))) <= spec.fuel().aimToleranceDegrees();
        Vec direction = onTarget ? toKuudra : look;
        shots.add(new Shot(new Projectile(instance, player, muzzle, direction.mul(spec.fuel().boltSpeed()),
                ItemStack.of(Material.SPECTRAL_ARROW), 2.5f, 0, 80), ShotKind.BOLT));
        playAt(muzzle, "entity.arrow.shoot", 0.6f);
        spendFuel();
    }

    double ballistaDamage(SkyBlockPlayer player) {
        double damage = spec.boss().shotDamage().get(tier) * (1 + perks.fraction(player.getUuid(), "STEADY_AIM"));
        if (bossState.isStunned()) return damage * spec.stun().stunnedShotFactor();
        return tierSpec.stunRequired() ? damage * spec.boss().unstunnedShotFactor() : damage;
    }

    private boolean hitsKuudra(Pos position) {
        if (boss == null) return false;
        Pos base = boss.base();
        double half = boss.width() / 2;
        return Math.abs(position.x() - base.x()) <= half + 0.5 && Math.abs(position.z() - base.z()) <= half + 0.5
                && position.y() >= base.y() - 0.5 && position.y() <= base.y() + boss.height() + 0.5;
    }

    private void tickShots() {
        for (Shot shot : shots) {
            Projectile projectile = shot.projectile();
            boolean landed = shot.kind() == ShotKind.BOLT
                    ? projectile.tick(this::hitsKuudra)
                    : projectile.tick(position -> mobAt(position) != null);
            if (!landed) continue;
            shots.remove(shot);
            if (shot.kind() == ShotKind.BOLT) landBolt(projectile);
            else landCannonball(projectile);
        }
    }

    private void landBolt(Projectile bolt) {
        if (!hitsKuudra(bolt.position()) || !bossState.canBeShot()) return;
        ring(bolt.position(), 1.5, Particle.LAVA);
        playAt(bolt.position(), "entity.generic.explode", 0.7f);
        tokens.gain(spec.boss().shotTokens());
        bossState.shoot(ballistaDamage(bolt.shooter()));
        afterDamage();
    }

    /** Staff damage in percent of Kuudra's health. */
    public void damageKuudra(double percent) {
        synchronized (run) {
            damageKuudraPoints(bossState.maxHealth() * percent / 100);
        }
    }

    void damageKuudraPoints(double points) {
        if (finishing) return;
        bossState.damage(points);
        afterDamage();
    }

    /** At 0 he dies (or, in the lair, goes down for good); on Infernal 25% opens the lair. */
    private void afterDamage() {
        if (finishing || stopped) return;
        if (bossState.health() <= 0) {
            if (lair != null && lair.entered()) lairKilled();
            else beginDeath();
            return;
        }
        if (lair != null && !lair.started() && bossState.atFloor()) lair.begin();
    }

    /** Elle's three lines while he sinks, then KUUDRA DOWN (the Burning capture: 15.5 s). */
    private void beginDeath() {
        finishing = true;
        closeStomach();
        bossState.setCycling(false);
        bossState.sink();
        deathFinale = KuudraFinale.death(spec.death());
    }

    private void lairKilled() {
        finishing = true;
        if (boss != null) {
            for (int i = 0; i < LAIR_DEATH_BLASTS; i++) {
                instance.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.HOSTILE, 1.5f, 0.49f),
                        boss.base().x(), boss.base().y(), boss.base().z());
            }
        }
        if (lair != null) lair.stop();
        bossState.setCycling(false);
        bossState.sink();
        later(spec.death().lairFinishTicks(), () -> run.finish(net.swofty.type.kuudra.run.KuudraRunState.Outcome.WIN));
    }

    void closeStomach() {
        if (stunCycle != null) stunCycle.close();
    }

    // ---- cannons ---------------------------------------------------------------------------------------------

    private void useCannon(SkyBlockPlayer player) {
        synchronized (run) {
            if (stopped || lives.isGhost(player.getUuid()) || carrying.containsKey(player.getUuid())) return;
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

    private void fireCannon(SkyBlockPlayer player, KuudraCannon cannon) {
        UUID uuid = player.getUuid();
        double cooldownSeconds = perks.value(uuid, "RAPID_FIRE") > 0 ? perks.value(uuid, "RAPID_FIRE") : spec.cannons().cooldownSeconds();
        if (tick < cannonReady.getOrDefault(uuid, 0L)) return;
        cannonReady.put(uuid, tick + Math.round(cooldownSeconds * 20));
        int balls = perks.value(uuid, "MULTI_SHOT") > 0 ? (int) perks.value(uuid, "MULTI_SHOT") : 1;
        double speed = spec.cannons().speed() * (1 + perks.fraction(uuid, "ACCELERATED_SHOT"));
        Vec look = player.getPosition().direction().normalize();
        for (int i = 0; i < balls; i++) {
            Vec direction = i == 0 ? look : look.rotateAroundY((ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3)
                    .add(0, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1, 0).normalize();
            shots.add(new Shot(new Projectile(instance, player, cannon.muzzle(player), direction.mul(speed),
                    ItemStack.of(Material.COAL_BLOCK), 0.6f, 0.04, 100), ShotKind.CANNONBALL));
        }
        playAt(cannon.position(), "entity.generic.explode", 0.5f);
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
        ring(ball.position(), radius, Particle.EXPLOSION);
        playAt(ball.position(), "entity.generic.explode", 1f);
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

    public void knockOffCannon(SkyBlockPlayer player) {
        cannons.forEach(cannon -> {
            if (cannon.isRiding(player)) cannon.dismount();
        });
    }

    // ---- input from events -----------------------------------------------------------------------------------

    /** A left-click swing; true when the fight used it (firing a seat), so abilities must not also trigger. */
    public boolean swing(SkyBlockPlayer player) {
        synchronized (run) {
            if (stopped) return false;
            if (stunCycle != null && stunCycle.isInside(player)) return stunCycle.swing(player);
            if (ballista != null && ballista.isRiding(player)) {
                fireBallista(player);
                return true;
            }
            for (KuudraCannon cannon : cannons) {
                if (cannon.isRiding(player)) {
                    fireCannon(player, cannon);
                    return true;
                }
            }
            return carrying.containsKey(player.getUuid());
        }
    }

    /** A left-click on an entity: building a pile, lifting cargo, the Human Cannonball, or hitting Kuudra. */
    public void attack(SkyBlockPlayer player, Entity target) {
        synchronized (run) {
            if (stopped) return;
            if (ballista != null) {
                String pile = ballista.pileOf(target);
                if (pile != null) {
                    hitPile(pile, player);
                    return;
                }
            }
            for (LavaCargo item : cargo) {
                if (item.owns(target)) {
                    useCargo(item, player);
                    return;
                }
            }
            for (KuudraCannon cannon : cannons) {
                if (cannon.owns(target)) {
                    humanCannonball(player);
                    return;
                }
            }
            if (boss != null && boss.is(target)) hitKuudra(player);
        }
    }

    private void hitKuudra(SkyBlockPlayer player) {
        weaponHit(player, PlayerStatistics.runPrimaryDamageFormula(player.getStatistics().allStatistics(),
                ItemStatistics.builder().build()).getKey(), false);
    }

    /** An arrow (or a Salvation beam) from the player landed on Kuudra; each one is its own hit. */
    public void arrowHitKuudra(SkyBlockPlayer player, double damage) {
        synchronized (run) {
            if (!stopped) weaponHit(player, damage, true);
        }
    }

    /** A melee or bow hit: real damage in the lair, a counted hit while stunned, otherwise Elle's "futile" line. */
    private void weaponHit(SkyBlockPlayer player, double damage, boolean arrow) {
        if (lives.isGhost(player.getUuid()) || finishing) return;
        if (lair != null && lair.entered()) {
            lair.hit(player, damage * CombatPetHooks.kuudraDamageMultiplier(player));
            return;
        }
        if (stunCycle != null && stunCycle.stunnedHit(player, arrow)) return;
        if (spec.boss().playerHitsDamage()) return;
        long ready = futileReady.getOrDefault(player.getUuid(), 0L);
        if (tick < ready) return;
        futileReady.put(player.getUuid(), tick + spec.boss().futileCooldownSeconds() * 20L);
        dialogue.chat("futile", ThreadLocalRandom.current()).forEach(player::sendMessage);
    }

    private void humanCannonball(SkyBlockPlayer player) {
        if (lives.isGhost(player.getUuid()) || !perks.of(player.getUuid()).useHumanCannonball()) return;
        playAt(player.getPosition(), "entity.generic.explode", 1f);
        if (stunCycle != null && stunCycle.fireIntoMouth(player)) return;
        Vec look = player.getPosition().direction().normalize();
        player.setVelocity(look.mul(35).withY(22));
        player.sendMessage("<a>You were fired out of the cannon!");
    }

    public void sneak(SkyBlockPlayer player) {
        synchronized (run) {
            if (ballista != null && ballista.isRiding(player)) ballista.dismount();
            knockOffCannon(player);
        }
    }

    /** Right-click with a run item; true when it was one. */
    public boolean useItem(SkyBlockPlayer player, ItemStack held) {
        KuudraItems.Kind kind = KuudraItems.kindOf(held);
        if (kind == null) return false;
        synchronized (run) {
            if (stopped) return true;
            switch (kind) {
                case ELLES_LAVA_ROD -> {
                    if (rod != null && !lives.isGhost(player.getUuid())) rod.use(player);
                }
                case REMOTE_PERK_SHOP -> openPerkShop(player);
                case MINIATURE_NUKE -> {
                    if (lives.isGhost(player.getUuid()) || !KuudraItems.consumeHeld(player, kind)) return true;
                    double range = spec.support().nukeRange();
                    ring(player.getPosition(), range / 2, Particle.EXPLOSION_EMITTER);
                    playAt(player.getPosition(), "entity.generic.explode", 0.5f);
                    for (KuudraMob mob : List.copyOf(mobs)) {
                        if (!mob.isDead() && mob.getPosition().distance(player.getPosition()) <= range) {
                            mob.damage(new EntityDamage(player, (float) (mob.getMaxHealth() * spec.support().nukeFraction())));
                        }
                    }
                }
                case ELLES_PICKAXE -> {
                }
            }
            return true;
        }
    }

    public void openPerkShop(SkyBlockPlayer player) {
        player.openView(new GUIKuudraPerkShop(this));
    }

    /** Carrying locks the hotbar and abilities (research K1 3.1). */
    public boolean locksHands(SkyBlockPlayer player) {
        return carrying.containsKey(player.getUuid());
    }

    public void damaged(SkyBlockPlayer player, EntityDamageEvent event) {
        synchronized (run) {
            if (stopped || run.state().phase() == null || lives.isInvulnerable(player.getUuid(), tick)) {
                event.setCancelled(true);
                return;
            }
            double reduction = 0;
            for (SkyBlockPlayer supporter : alivePlayers()) {
                if (supporter.getPosition().distance(player.getPosition()) <= spec.support().protectiveAuraRange()) {
                    reduction = Math.max(reduction, perks.fraction(supporter.getUuid(), "PROTECTIVE_AURA"));
                }
            }
            if (reduction > 0) event.getDamage().setAmount((float) (event.getDamage().getAmount() * (1 - reduction)));
            if (carrying.containsKey(player.getUuid()) && CargoCarry.dropsOnHit(event.getDamage().getAmount(),
                    player.getMaxHealth(), spec.supplies().dropDamageFraction())) {
                dropCarried(player.getUuid(), true);
            }
        }
    }

    // ---- deaths ----------------------------------------------------------------------------------------------

    /** Handles a death in the run; true always, so the normal SkyBlock death never happens here. */
    public boolean death(SkyBlockPlayer player) {
        synchronized (run) {
            return death(player, false);
        }
    }

    /** In the Infernal lair every death is a final kill (research K1 3.5; the Infernal capture). */
    private boolean death(SkyBlockPlayer player, boolean forcedFinal) {
        player.setHealth(player.getMaxHealth());
        if (stopped || run.state().phase() == null) return true;
        if (lives.isGhost(player.getUuid())) return true;
        boolean finalKill = forcedFinal || (lair != null && lair.entered()) || (player.getLastDamageSource() != null
                && player.getLastDamageSource().getAttacker() instanceof KuudraRushers.Chaosmite);
        boolean autoRevive = !finalKill && perks.of(player.getUuid()).autoRevives() > 0;
        KuudraLives.Death death = lives.die(player.getUuid(), tick, finalKill, autoRevive, fasterRespawn());
        if (death == KuudraLives.Death.AUTO_REVIVED) {
            perks.of(player.getUuid()).useAutoRevive();
            player.setHealth((float) (player.getMaxHealth() * spec.lives().autoReviveHealth()));
            player.sendMessage("<a>Your Auto Revive brought you back to life!");
            return true;
        }
        ghost(player, death == KuudraLives.Death.FINAL);
        if (lives.wiped(run.onlineMembers().stream().map(SkyBlockPlayer::getUuid).toList())) {
            run.finish(net.swofty.type.kuudra.run.KuudraRunState.Outcome.LOSE);
        }
        return true;
    }

    private double fasterRespawn() {
        return run.onlineMembers().stream().mapToDouble(member -> perks.fraction(member.getUuid(), "FASTER_RESPAWN")).max().orElse(0);
    }

    /** Kills a player outright from a lair telegraph. */
    void killInLair(SkyBlockPlayer player) {
        death(player, true);
    }

    private void ghost(SkyBlockPlayer player, boolean finalKill) {
        release(player);
        if (stunCycle != null) stunCycle.leave(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);
        for (SkyBlockPlayer member : run.onlineMembers()) {
            member.sendMessage(member == player ? dialogue.announcement("you_died", Map.of())
                    : dialogue.announcement("died", Map.of("player", player.getUsername())));
        }
        if (finalKill) run.broadcast(dialogue.announcement("final_killed", Map.of("player", player.getUsername())));
        player.sendMessage(finalKill
                ? "<c>You were final killed! Only a Support's revive can bring you back."
                : "<c>You died! You will be respawned in " + lives.secondsToRespawn(player.getUuid(), tick) + " seconds.");
        player.playSound(Sound.sound(Key.key("entity.generic.hurt"), Sound.Source.NEUTRAL, 1f, 0.89f));
        player.showTitle(Text.parse(finalKill ? "<c><l>FINAL KILLED" : "<e>You became a ghost!"),
                Text.parse("<7>Hopefully your teammates will be able to revive you!"),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500)));
    }

    /** "You will be revived in 5s" ... "1s" under the ghost title (the Infernal capture). */
    private void tickReviveTitles(List<SkyBlockPlayer> online) {
        for (SkyBlockPlayer player : online) {
            if (!lives.isGhost(player.getUuid()) || lives.isFinal(player.getUuid())) continue;
            int seconds = lives.secondsToRespawn(player.getUuid(), tick);
            if (seconds < 1 || seconds > REVIVE_TITLE_SECONDS) continue;
            player.showTitle(Text.empty(), Text.parse("<a>You will be revived in " + seconds + "s"),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO));
        }
    }

    private void respawn(SkyBlockPlayer player) {
        unghost(player);
        player.teleport(lair != null && lair.entered() && arena.lair() != null ? arena.lair().landing() : arena.respawn());
        player.sendMessage("<a>You have respawned!");
    }

    private void unghost(SkyBlockPlayer player) {
        lives.revive(player.getUuid(), tick);
        player.setInvisible(false);
        player.setFlying(false);
        player.setAllowFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
    }

    public boolean revive(SkyBlockPlayer reviver, boolean finalKilled) {
        synchronized (run) {
            for (SkyBlockPlayer member : run.onlineMembers()) {
                if (!lives.isGhost(member.getUuid()) || lives.isFinal(member.getUuid()) != finalKilled) continue;
                respawn(member);
                run.broadcast(reviver.getFullDisplayName() + " <a>revived " + member.getFullDisplayName() + "<a>!");
                return true;
            }
            return false;
        }
    }

    public boolean hasGhost(boolean finalKilled) {
        return run.onlineMembers().stream().anyMatch(member -> lives.isGhost(member.getUuid()) && lives.isFinal(member.getUuid()) == finalKilled);
    }

    // ---- waves and minions -----------------------------------------------------------------------------------

    public void spawnWave(int number) {
        synchronized (run) {
            if (stopped || !emerged || (lair != null && lair.started())) return;
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
                Pos groupFrom = minionSpawn();
                Pos groupLanding = landingFor(groupFrom);
                for (int i = 0; i < count; i++) {
                    String mobId = batch.mobId();
                    Pos from = mobId.equals("MAGMA_FOLLOWER") ? groupFrom : minionSpawn();
                    Pos landing = switch (mobId) {
                        case "MAGMA_FOLLOWER" -> scatter(groupLanding, 3);
                        case "WANDERING_BLAZE" -> boss != null && bossState.stance() == KuudraBossState.Stance.SURFACED
                                ? boss.mouth() : landingFor(from).add(0, 12, 0);
                        case "WITHER_SENTRY" -> randomPlatformPoint(8);
                        case "DROPSHIP" -> centre().add(loopRadius(), spec.mobs().dropshipAltitude(), 0);
                        default -> landingFor(from);
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

    public @Nullable KuudraMob spawnMob(String mobId, Pos at) {
        return spawnMob(mobId, at, new ArrayList<>());
    }

    private @Nullable KuudraMob spawnMob(String mobId, Pos at, List<KuudraSpecialists.MagmaFollower> group) {
        if (stopped) return null;
        KuudraMobSpec mobSpec = config.mob(mobId);
        if (mobSpec == null) return null;
        KuudraMob mob = KuudraMobs.create(this, mobSpec, group);
        if (mob == null) return null;
        mobs.add(mob);
        mob.setInstance(instance, at);
        return mob;
    }

    private void spawnTentacle() {
        KuudraFightSpec.Tentacles tentacles = spec.tentacles();
        KuudraMobSpec tentacleSpec = KuudraTentacle.spec(tier, tentacles.health().get(tier), tentacles.slamDamage().get(tier),
                config.tentacleTokens());
        KuudraTentacle tentacle = new KuudraTentacle(this, tentacleSpec);
        mobs.add(tentacle);
        List<KuudraArena.SupplySpot> spots = new ArrayList<>(arena.supplySpots().values());
        Pos near = spots.get(ThreadLocalRandom.current().nextInt(spots.size())).position();
        tentacle.setInstance(instance, arena.lavaBeside(near.add(ThreadLocalRandom.current().nextDouble(-10, 10), 0,
                ThreadLocalRandom.current().nextDouble(-10, 10)), 1.5).add(0, -2, 0));
    }

    private void tickTentacleRespawns() {
        for (Long due : tentacleRespawns) {
            if (due > tick) continue;
            tentacleRespawns.remove(due);
            spawnTentacle();
        }
    }

    public void slam(KuudraTentacle tentacle) {
        if (slams == null || stopped) return;
        List<SkyBlockPlayer> targets = alivePlayers().stream().filter(player -> arena.onPlatform(player.getPosition())).toList();
        Pos toward = targets.isEmpty() ? centre() : targets.get(ThreadLocalRandom.current().nextInt(targets.size())).getPosition();
        Pos from = tentacle.getPosition().withY(arena.floorY());
        slams.start(new TentacleSlams.Slam(TentacleSlams.path(from, toward, spec.tentacles().pathLength(),
                spec.tentacles().pathWidth()), tentacle));
        playAt(from, "entity.ravager.roar", 0.6f);
        for (LavaCargo item : cargo) {
            CargoPickup pickup = item.pickup();
            if (pickup == null) continue;
            SkyBlockPlayer player = member(pickup.player());
            if (player == null || player.getPosition().distance(tentacle.getPosition()) > spec.tentacles().grabRange()) continue;
            item.setPickup(null);
            player.setVelocity(new Vec(0, 25, 0));
            player.sendMessage("<c>A tentacle snatched you away from the supplies!");
        }
    }

    /** One of "One last flail"'s sweeps from the platform edge across the centre (the Infernal capture). */
    void flailSlam(Pos from, double damage) {
        if (slams == null || stopped) return;
        Pos edge = from.withY(arena.floorY());
        int length = (int) Math.ceil(horizontal(edge, centre()) * 2);
        KuudraTentacle source = tentacles().stream().findFirst().orElse(null);
        slams.start(new TentacleSlams.Slam(TentacleSlams.path(edge, centre(), length, spec.lair().pathWidth()),
                new FlailSource(source, damage)));
        playAt(edge, "entity.ravager.roar", 0.6f);
    }

    private void landSlam(TentacleSlams.Slam slam) {
        if (slam.source() instanceof FlailSource(KuudraTentacle tentacle, double damage)) {
            if (tentacle == null || tentacle.isDead() || tentacle.isRemoved()) return;
            for (SkyBlockPlayer player : alivePlayers()) {
                Pos at = player.getPosition();
                if (TentacleSlams.covers(slam, at) && at.y() < arena.floorY() + 3) MobAttackService.trueStrike(tentacle, player, damage);
            }
            return;
        }
        if (!(slam.source() instanceof KuudraTentacle tentacle) || tentacle.isDead() || tentacle.isRemoved()) return;
        double damage = spec.tentacles().slamDamage().get(tier);
        for (SkyBlockPlayer player : alivePlayers()) {
            Pos at = player.getPosition();
            if (TentacleSlams.covers(slam, at) && at.y() < arena.floorY() + 3) MobAttackService.trueStrike(tentacle, player, damage);
        }
        TentacleSlams.Column middle = slam.path().get(slam.path().size() / 2);
        ring(new Pos(middle.x() + 0.5, arena.floorY(), middle.z() + 0.5), 2, Particle.EXPLOSION);
        playAt(new Pos(middle.x(), arena.floorY(), middle.z()), "entity.generic.explode", 0.8f);
        if (build != null && ballista != null && run.state().phase() == KuudraPhase.BUILD) {
            boolean nearBallista = slam.path().stream().anyMatch(column ->
                    Math.hypot(column.x() + 0.5 - ballista.base().x(), column.z() + 0.5 - ballista.base().z()) <= spec.build().mobHitRange());
            if (nearBallista && build.knockAny(spec.tentacles().buildKnockback(), ThreadLocalRandom.current()) != null) barkBallistaDamaged();
        }
    }

    public void forget(KuudraMob mob) {
        if (!mobs.remove(mob)) return;
        if (mob instanceof KuudraTentacle && !stopped) tentacleRespawns.add(tick + spec.tentacles().respawnSeconds() * 20L);
    }

    public void onMobKilled(KuudraMob mob, SkyBlockPlayer killer) {
        synchronized (run) {
            tokens.gain(mob.tokens());
            double chance = perks.fraction(killer.getUuid(), "FREEZING_TOUCH");
            if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
                for (KuudraMob other : mobs) {
                    if (other.getPosition().distance(mob.getPosition()) <= spec.support().freezingTouchRange()) {
                        other.freeze(spec.mobs().freezeSeconds());
                    }
                }
                ring(mob.getPosition(), spec.support().freezingTouchRange(), Particle.SNOWFLAKE);
            }
        }
    }

    /** Sweeping Edge: part of every hit lands on the other minions nearby. */
    public void onMobHit(SkyBlockPlayer player, KuudraMob mob, double dealt) {
        double share = perks.fraction(player.getUuid(), "SWEEPING_EDGE");
        if (share <= 0) return;
        for (KuudraMob other : List.copyOf(mobs)) {
            if (other == mob || other.isDead() || other.getPosition().distance(mob.getPosition()) > spec.support().sweepingEdgeRange()) continue;
            other.damage(new EntityDamage(player, (float) (dealt * share)));
        }
    }

    /** Bonus Damage, Vulcan's Kuudra Master, and Blight Slayer's share against a red Blight. */
    public double outgoingMultiplier(SkyBlockPlayer player, KuudraMob mob) {
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

    public void mergeMagmaFollowers(List<KuudraSpecialists.MagmaFollower> group, Pos at) {
        group.forEach(KuudraMob::remove);
        spawnMob("OMEGAGMA", at);
    }

    public void explode(Pos at, double radius, double damage, KuudraMob source) {
        ring(at, radius, Particle.EXPLOSION);
        playAt(at, "entity.generic.explode", 1f);
        for (SkyBlockPlayer player : alivePlayers()) {
            if (player.getPosition().distance(at) <= radius) MobAttackService.strike(source, player, damage);
        }
    }

    public void primeTnt(Pos at, int fuseTicks, double damage, KuudraMob source) {
        Entity tnt = new Entity(EntityType.TNT);
        tnt.editEntityMeta(PrimedTntMeta.class, meta -> meta.setFuseTime(fuseTicks));
        tnt.setInstance(instance, at);
        later(fuseTicks, () -> {
            Displays.remove(tnt);
            explode(at, spec.mobs().explosionRadius(), damage, source);
        });
    }

    public void dropshipBomb(KuudraMob ship, double damage) {
        ring(centre(), 6, Particle.EXPLOSION_EMITTER);
        playAt(centre(), "entity.generic.explode", 0.6f);
        for (SkyBlockPlayer player : alivePlayers()) MobAttackService.strike(ship, player, damage);
    }

    // ---- auras, HUD and helpers ------------------------------------------------------------------------------

    private void tickAuras() {
        List<SkyBlockPlayer> alive = alivePlayers();
        var support = spec.support();
        for (SkyBlockPlayer supporter : alive) {
            double healing = perks.fraction(supporter.getUuid(), "HEALING_AURA");
            double mana = perks.fraction(supporter.getUuid(), "MANA_AURA");
            if (healing <= 0 && mana <= 0) continue;
            for (SkyBlockPlayer ally : alive) {
                double distance = ally.getPosition().distance(supporter.getPosition());
                if (healing > 0 && distance <= support.healingAuraRange()) {
                    ally.setHealth((float) Math.min(ally.getMaxHealth(), ally.getHealth() + ally.getMaxHealth() * healing));
                }
                if (mana > 0 && distance <= support.manaAuraRange()) {
                    float max = ally.getMaxMana();
                    ally.setMana((float) Math.min(max, ally.getMana() + max * support.manaRegenBase() / 100 * mana));
                }
            }
        }
    }

    private void tickActionBars(List<SkyBlockPlayer> online) {
        for (SkyBlockPlayer player : online) {
            if (lives.isGhost(player.getUuid())) {
                int seconds = lives.secondsToRespawn(player.getUuid(), tick);
                actionBar(player, seconds < 0 ? "<c>FINAL KILLED" : "<c>Respawning in <e>" + seconds + "s");
            }
        }
    }

    private void actionBar(SkyBlockPlayer player, String markup) {
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE, Text.parse(markup), 6, 20);
    }

    public Hud hud(SkyBlockPlayer player) {
        KuudraPhase phase = run.state().phase();
        String progress = null;
        if (phase == KuudraPhase.SUPPLIES) progress = suppliesPlaced + "/" + spec.supplies().crates();
        else if (phase == KuudraPhase.BUILD && build != null) progress = build.overallPercent() + "%";
        else if (phase == KuudraPhase.FUEL && !fuel.armed() && stunCycle == null) progress = fuel.loaded() + "/" + fuel.cells();
        String status = null;
        if (lives.isGhost(player.getUuid())) status = lives.isFinal(player.getUuid()) ? "<c>FINAL KILLED" : "<c>DEAD";
        boolean lairStarted = lair != null && lair.started();
        boolean timer = (fuel.everLoaded() || (stunCycle != null && stunCycle.begun())) && !finishing && !lairStarted;
        return new Hud(tokens.balance(player.getUuid()), progress, timer ? bossState.secondsUntilSubmerge() : -1, status,
                !finishing && !lairStarted, !lairStarted);
    }

    private double organ(SkyBlockPlayer player, Map<String, Double> bonuses) {
        double best = 0;
        for (Map.Entry<String, Double> organ : bonuses.entrySet()) {
            ItemType type = ItemType.get(organ.getKey());
            if (type != null && UsableAccessory.usable(player, type)) best = Math.max(best, organ.getValue() / 100);
        }
        return best;
    }

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

    public Pos centre() {
        return arena.centre();
    }

    public double loopRadius() {
        KuudraArena.Box platform = arena.platform();
        return Math.max(platform.maxX() - platform.minX(), platform.maxZ() - platform.minZ()) / 2.0 + 4;
    }

    /** A random spot on the main platform's floor at least {@code inset} blocks in from the bounding box's edge. */
    public Pos randomPlatformPoint(double inset) {
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
    private Pos minionSpawn() {
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

    private Pos scatter(Pos around, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 6; attempt++) {
            Pos at = around.add(random.nextDouble(-radius, radius), 0, random.nextDouble(-radius, radius));
            if (onMainPlatform(at)) return at;
        }
        return around;
    }

    /** Minions jump out of the lava onto the platform in an arc (the captures). */
    private void leap(KuudraMob mob, Pos from, Pos to) {
        leaps.removeIf(each -> each.mob() == mob);
        mob.setNoGravity(true);
        leaps.add(new Leap(mob, from, to, tick));
    }

    private void tickLeaps() {
        for (Leap leap : leaps) {
            KuudraMob mob = leap.mob();
            if (mob.isDead() || mob.isRemoved()) {
                leaps.remove(leap);
                continue;
            }
            if (mob.getInstance() == null) continue;
            double t = Math.min(1, (tick - leap.start()) / (double) LEAP_TICKS);
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
    private void rescueFallenMobs() {
        if (lair != null && lair.started()) return;
        for (KuudraMob mob : mobs) {
            if (mob instanceof KuudraTentacle || mob.isDead() || FLYERS.contains(mob.spec().id())) continue;
            if (leaps.stream().anyMatch(leap -> leap.mob() == mob)) continue;
            Pos at = mob.getPosition();
            if (at.y() >= arena.floorY() - 1.5 || at.y() < arena.lavaSurfaceY() - 8) continue;
            if (!arena.sea().containsColumn(at.blockX(), at.blockZ())) continue;
            leap(mob, at, landingFor(at));
        }
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
    private Pos edgePoint() {
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

    public void ring(Pos at, double radius, Particle particle) {
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

    private static double horizontal(Pos a, Pos b) {
        return Math.hypot(a.x() - b.x(), a.z() - b.z());
    }

    /** The centred end-of-run block from the Burning capture: KUUDRA DOWN, completion, time and tokens. */
    public List<String> summary(SkyBlockPlayer player, boolean win) {
        String bar = "<a><l>" + "▬".repeat(64);
        long seconds = run.state().elapsedSeconds();
        int complete = win ? 100 : (int) Math.floor((1 - bossState.fraction()) * 100);
        return List.of(bar,
                "<f>" + " ".repeat(31) + (win ? "<6><l>KUUDRA DOWN!" : "<c><l>DEFEAT"),
                "<f>" + " ".repeat(24) + "<8>Percentage Complete: <f>" + complete + "%",
                "<f>" + " ".repeat(32) + "Time: <a>" + String.format("%02dm %02ds", seconds / 60, seconds % 60),
                "<f>" + " ".repeat(27) + "Tokens Earned: <5>" + tokens.earned(player.getUuid()),
                bar);
    }

    // ---- staff controls --------------------------------------------------------------------------------------

    public void completeSupplies() {
        synchronized (run) {
            if (run.state().phase() == KuudraPhase.SUPPLIES) run.advancePhase();
        }
    }

    public void completeBuild() {
        synchronized (run) {
            if (build != null && run.state().phase() == KuudraPhase.BUILD) build.completeAll();
        }
    }

    public void completeFuel() {
        synchronized (run) {
            if (run.state().phase() != KuudraPhase.FUEL) return;
            fuel.fill();
            clearCargo();
            run.advancePhase();
        }
    }

    public void giveTokens(int amount) {
        synchronized (run) {
            tokens.gain(amount);
        }
    }

    // ---- accessors for the Perk Shop and minions -------------------------------------------------------------

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
        return mobs.stream().filter(mob -> mob instanceof KuudraTentacle && !mob.isDead())
                .map(mob -> (KuudraTentacle) mob).toList();
    }

    Pos randomEdge() {
        return edgePoint();
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

    // ---- staff controls for the stun cycle and the lair ------------------------------------------------------

    public enum StaffResult {DONE, NOT_A_STUN_TIER, NOT_INFERNAL, NOT_READY}

    private StaffResult stunReady() {
        if (stunCycle == null) return StaffResult.NOT_A_STUN_TIER;
        if (!emerged || finishing()) return StaffResult.NOT_READY;
        KuudraPhase phase = run.state().phase();
        if (phase == KuudraPhase.SUPPLIES || phase == KuudraPhase.BUILD) {
            if (build == null) build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
            run.enterPhase(KuudraPhase.FUEL);
        }
        stunCycle.begin();
        if (bossState.stance() != KuudraBossState.Stance.SURFACED) {
            bossState.setCycling(true);
            bossState.rise();
            return StaffResult.NOT_READY;
        }
        return StaffResult.DONE;
    }

    /** Swallows the player (staff), skipping ahead to the stun cycle when needed. */
    public StaffResult eat(SkyBlockPlayer player) {
        synchronized (run) {
            StaffResult ready = stunReady();
            if (ready != StaffResult.DONE) return ready;
            stunCycle.stomach().setOpen(true);
            return stunCycle.eat(player) ? StaffResult.DONE : StaffResult.NOT_READY;
        }
    }

    /** Breaks a pod as if the player mined it (staff), stunning Kuudra. */
    public StaffResult stun(SkyBlockPlayer player) {
        synchronized (run) {
            StaffResult ready = stunReady();
            if (ready != StaffResult.DONE) return ready;
            return stunCycle.breakAPod(player) ? StaffResult.DONE : StaffResult.NOT_READY;
        }
    }

    /** Spits the player out of Kuudra's mouth (staff). */
    public StaffResult spit(SkyBlockPlayer player) {
        synchronized (run) {
            if (stunCycle == null) return StaffResult.NOT_A_STUN_TIER;
            if (!stunCycle.isInside(player)) return StaffResult.NOT_READY;
            stunCycle.launchOut(player);
            return StaffResult.DONE;
        }
    }

    /** Takes Kuudra to 25% and starts the Infernal skip; a second use plays the rest of it at once (staff). */
    public StaffResult lair() {
        synchronized (run) {
            if (lair == null) return StaffResult.NOT_INFERNAL;
            if (!emerged || finishing) return StaffResult.NOT_READY;
            if (lair.started()) {
                lair.skip();
                return StaffResult.DONE;
            }
            bossState.damage(bossState.maxHealth());
            afterDamage();
            return lair.started() ? StaffResult.DONE : StaffResult.NOT_READY;
        }
    }
}
