package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.CatacombsRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.dungeons.catacombs.run.DungeonRunRules;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.type.catacombs.arena.CatacombsArena;
import dev.daweapon.type.catacombs.classes.ClassHost;
import dev.daweapon.type.catacombs.classes.RunClasses;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.catacombs.engine.MobDeaths;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.catacombs.engine.KeyRing;
import dev.daweapon.type.game.game.AbstractGame;
import dev.daweapon.type.game.game.event.PlayerPostJoinGameEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.Clock;
import net.minestom.server.item.ItemStack;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** One party's Catacombs run in its own placeholder dungeon. */
public final class CatacombsRun extends AbstractGame<CatacombsParticipant> implements DungeonHost, ClassHost {
    public static final int SHUTDOWN_SECONDS = 60;
    private static final int POLL_TICKS = 5;
    private static final int TRANSFER_GRACE_TICKS = 200;
    private static final int AUTO_READY_TICKS = 66;

    private final CatacombsFloorId floorId;
    private final CatacombsFloorDefinition definition;
    private final Set<UUID> roster;
    private final UUID creator;
    private final long seed;
    private final long createdAt = System.nanoTime();
    private final Set<UUID> departed = ConcurrentHashMap.newKeySet();
    private final Set<UUID> leavers = ConcurrentHashMap.newKeySet();
    private final CatacombsReadyCheck readyCheck = new CatacombsReadyCheck();
    private final CatacombsArena arena;
    private final CatacombsLifecycle lifecycle;
    private final GhostRoster ghosts;
    private final KeyRing keys = new KeyRing();
    private final RunClasses classes = new RunClasses(this);
    private final RunStats stats = new RunStats();
    private static final int SCORE_MAP_DELAY_TICKS = 10;
    private final Map<UUID, ScoreScreen.Earned> earned = new ConcurrentHashMap<>();
    private volatile long finishedSeconds;
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private volatile long emptySince = System.currentTimeMillis();
    private volatile @Nullable DungeonEngine engine;
    private final Map<UUID, ItemStack> scoreMaps = new ConcurrentHashMap<>();

    CatacombsRun(CatacombsFloorId floorId, List<UUID> roster, CatacombsArena arena, long seed) {
        super(arena.instance(), _ -> {
        });
        CatacombsRoster.validated(roster);
        this.floorId = floorId;
        this.definition = CatacombsRegistry.defaults().floor(floorId.floor(), floorId.mode());
        this.roster = Set.copyOf(roster);
        this.creator = roster.getFirst();
        this.arena = arena;
        this.seed = seed;
        Map<UUID, DungeonClassType> partyClasses = new HashMap<>();
        roster.forEach(member -> partyClasses.put(member, DungeonClassProgress.DEFAULT_CLASS));
        CatacombsRunConfig config = new CatacombsRunConfig(definition, partyClasses, arena.layout().totalRooms(),
                arena.layout().totalSecrets(), true);
        this.lifecycle = new CatacombsLifecycle(config, Clock.systemUTC(), SHUTDOWN_SECONDS);
        DungeonRunRules rules = definition.rules();
        this.ghosts = new GhostRoster(rules.automaticGhostRevive(), DungeonRunRules.AUTOMATIC_REVIVE_SECONDS);
    }

    void open() {
        MobDeaths.listen(arena.instance(), this::mobDied);
        tasks.add(MinecraftServer.getSchedulerManager().buildTask(this::tickSecond)
                .delay(TaskSchedule.seconds(1)).repeat(TaskSchedule.seconds(1)).schedule());
        tasks.add(MinecraftServer.getSchedulerManager().buildTask(this::poll)
                .delay(TaskSchedule.tick(POLL_TICKS)).repeat(TaskSchedule.tick(POLL_TICKS)).schedule());
    }

    @Override
    public int getMaxPlayers() {
        return roster.size();
    }

    @Override
    public int getMinPlayers() {
        return 1;
    }

    @Override
    public synchronized JoinResult join(CatacombsParticipant participant) {
        UUID uuid = participant.getUuid();
        if (!roster.contains(uuid)) return new JoinResult.Denied("You aren't in this Catacombs party");
        if (players.containsKey(uuid)) return new JoinResult.Denied("Player is already in this game");
        if (hasDisconnectedPlayer(uuid)) {
            return handleRejoin(participant) ? new JoinResult.Success() : new JoinResult.Denied("Couldn't rejoin the run");
        }
        CatacombsLifecycle.Stage stage = lifecycle.stage();
        if (stage != CatacombsLifecycle.Stage.WAITING && stage != CatacombsLifecycle.Stage.ACTIVE) {
            return new JoinResult.Denied("This Catacombs run is over");
        }
        players.put(uuid, participant);
        participant.setGameId(gameId);
        eventDispatcher.accept(new PlayerPostJoinGameEvent(this, participant.getServerPlayer(), players.size(), getMaxPlayers()));
        return new JoinResult.Success();
    }

    public boolean admit(SkyBlockPlayer player) {
        JoinResult result = join(new CatacombsParticipant(player));
        if (result instanceof JoinResult.Denied(String reason)) {
            player.sendMessage("<c>" + reason + "!");
            return false;
        }
        emptySince = 0;
        leavers.remove(player.getUuid());
        boolean waiting = lifecycle.stage() == CatacombsLifecycle.Stage.WAITING;
        player.setRespawnPoint(arena.entranceSpawn());
        player.setInstance(arena.instance(), waiting ? arena.entranceSpawn() : rejoinSpot()).thenRun(() -> {
            classes.admit(player);
            if (waiting) {
                if (DungeonClassProgress.autoReady(player)) later(AUTO_READY_TICKS, () -> readyUp(player));
            } else if (ghosts.isGhost(player.getUuid())) applyGhost(player);
            if (!waiting) classes.rejoin(player);
        });
        return true;
    }

    private Pos rejoinSpot() {
        return alivePlayers().stream().findFirst().map(Player::getPosition).orElse(arena.entranceSpawn());
    }

    public synchronized void handleDisconnect(UUID uuid) {
        if (!roster.contains(uuid)) return;
        CatacombsParticipant participant = players.get(uuid);
        if (participant != null) {
            handleDisconnect(participant);
            if (lifecycle.stage() == CatacombsLifecycle.Stage.ACTIVE) {
                leavers.add(uuid);
                broadcast(CatacombsMessages.disconnected(participant.player().getUsername()));
            }
        }
        if (onlineMembers().isEmpty() && emptySince == 0) emptySince = System.currentTimeMillis();
    }

    /** Staff's {@code /catacombsrun ready}: flips the ready state either way. */
    public synchronized void toggleReady(SkyBlockPlayer player) {
        if (!canChangeReady(player)) return;
        boolean ready = readyCheck.toggle(player.getUuid());
        broadcast(ready ? CatacombsMessages.ready(player.getColouredName()) : CatacombsMessages.notReady(player.getColouredName()));
        if (ready && readyCheck.startCountdownIfAllReady(onlineIds())) startCountdown();
    }

    /** Mort's Ready Up pane and Auto Ready Up: readying is one-way there, as the captured Ready pane offers no undo. */
    public synchronized void readyUp(SkyBlockPlayer player) {
        if (!canChangeReady(player) || player.getInstance() != arena.instance() || !readyCheck.markReady(player.getUuid())) return;
        broadcast(CatacombsMessages.ready(player.getColouredName()));
        if (readyCheck.startCountdownIfAllReady(onlineIds())) startCountdown();
    }

    private boolean canChangeReady(SkyBlockPlayer player) {
        return lifecycle.stage() == CatacombsLifecycle.Stage.WAITING && player.isOnline() && !readyCheck.countingDown();
    }

    public boolean isReady(UUID player) {
        return readyCheck.isReady(player);
    }

    private void startCountdown() {
        List<Integer> seconds = CatacombsReadyCheck.countdownSeconds();
        for (int index = 0; index < seconds.size(); index++) {
            String line = CatacombsMessages.startingIn(seconds.get(index));
            later(20 * index + 1, () -> broadcast(line));
        }
        later(20 * seconds.size(), () -> {
            if (lifecycle.stage() == CatacombsLifecycle.Stage.WAITING) begin();
        });
    }

    public synchronized boolean begin() {
        if (!lifecycle.begin()) return false;
        super.start();
        classes.begin(onlineMembers());
        DungeonEngine started = new DungeonEngine(this, arena.layout(), definition);
        engine = started;
        CatacombsMessages.dungeonEntered(floorId.floor(), floorId.mode()).forEach(this::broadcast);
        RunOpening.play(this, started);
        return true;
    }

    @Override
    public synchronized boolean advanceTo(DungeonRunPhase phase) {
        return lifecycle.advanceTo(phase);
    }

    public synchronized boolean finish(CatacombsLifecycle.Outcome outcome) {
        if (!lifecycle.finish(outcome)) return false;
        conclude(outcome);
        return true;
    }

    @Override
    public void win() {
        finish(CatacombsLifecycle.Outcome.WIN);
    }

    @Override
    public void lose(String reason) {
        synchronized (this) {
            if (lifecycle.stage() != CatacombsLifecycle.Stage.ACTIVE) return;
            broadcast(reason);
        }
        finish(CatacombsLifecycle.Outcome.LOSE);
    }

    public @Nullable ItemStack scoreMap(UUID player) {
        return scoreMaps.get(player);
    }

    private void conclude(CatacombsLifecycle.Outcome outcome) {
        super.end();
        DungeonEngine running = engine;
        if (running != null) running.dispose();
        onlineMembers().stream().filter(member -> ghosts.revive(member.getUuid())).forEach(this::releaseGhost);
        classes.conclude(onlineMembers());
        boolean win = outcome == CatacombsLifecycle.Outcome.WIN;
        CatacombsRunState state = lifecycle.state();
        if (state == null) {
            broadcast(CatacombsMessages.returning(SHUTDOWN_SECONDS));
            return;
        }
        DungeonScoreBreakdown score = state.score();
        finishedSeconds = state.elapsed().toSeconds();
        if (!win) {
            List<String> failed = ScoreScreen.failed(floorId.floor(), floorId.mode(), score, finishedSeconds);
            onlineMembers().forEach(member -> failed.forEach(member::sendMessage));
            broadcast(CatacombsMessages.returning(SHUTDOWN_SECONDS));
            return;
        }
        for (SkyBlockPlayer member : onlineMembers()) {
            ScoreScreen.Earned paid = reward(member, score);
            earned.put(member.getUuid(), paid);
        }
        CatacombsRewardChests.spawn(this, score, running == null ? null : running.rewardCentre(), running != null && running.realBossArena());
        for (SkyBlockPlayer member : onlineMembers()) {
            ScoreScreen.Earned paid = earned.get(member.getUuid());
            if (paid == null) continue;
            try {
                ScoreScreen.won(floorId.floor(), floorId.mode(), score, finishedSeconds, paid).forEach(member::sendMessage);
            } catch (RuntimeException exception) {
                org.tinylog.Logger.error(exception, "Score screen failed for {}", member.getUsername());
            }
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!member.isOnline() || member.getInstance() != instance()) return;
                ItemStack shown = ScoreSummaryMap.show(member, score);
                if (shown != null) scoreMaps.put(member.getUuid(), shown);
            }).delay(TaskSchedule.tick(SCORE_MAP_DELAY_TICKS)).schedule();
        }
    }

    private ScoreScreen.Earned reward(SkyBlockPlayer player, DungeonScoreBreakdown score) {
        try {
            long catacombs = CatacombsProgress.completeRun(player, floorId.floor(), floorId.mode(), score.total(), score.rank());
            Map<String, Long> byClass = new LinkedHashMap<>();
            DungeonClassType played = classes.classOf(player.getUuid());
            Map<DungeonClassType, Long> paid = classes.reward(player, floorId.floor());
            Long own = paid.get(played);
            if (own != null) byClass.put(played.displayName(), own);
            paid.forEach((type, amount) -> {
                if (type != played) byClass.put(type.displayName(), amount);
            });
            return new ScoreScreen.Earned(catacombs, byClass);
        } catch (Exception exception) {
            Logger.error(exception, "Couldn't pay {}'s Catacombs {} win", player.getUsername(), floorId.floor());
            player.sendMessage("<c>Something went wrong paying out your Catacombs rewards. Please report this to staff.");
            return new ScoreScreen.Earned(0, Map.of());
        }
    }

    /** What clicking EXTRA STATS on the score screen prints; false before the run has ended. */
    public boolean showExtraStats(SkyBlockPlayer player) {
        CatacombsRunState state = lifecycle.state();
        if (state == null || lifecycle.stage() != CatacombsLifecycle.Stage.ENDING) return false;
        UUID uuid = player.getUuid();
        ScoreScreen.Stats numbers = new ScoreScreen.Stats(classes.classOf(uuid).displayName(), stats.damageOf(uuid), 0,
                stats.killsOf(uuid), stats.deathsOf(uuid), state.secretsFound());
        ScoreScreen.extraStats(floorId.floor(), floorId.mode(), state.score(), finishedSeconds,
                lifecycle.outcome() == CatacombsLifecycle.Outcome.WIN, numbers).forEach(player::sendMessage);
        return true;
    }

    private void mobDied(CatacombsMob mob) {
        mob.getDamageDealt().forEach(stats::damage);
        SkyBlockPlayer killer = mob.lastHitter();
        if (killer != null) stats.kill(killer.getUuid());
    }

    public RunStats stats() {
        return stats;
    }

    public synchronized boolean death(SkyBlockPlayer player) {
        player.setHealth(player.getMaxHealth());
        CatacombsRunState state = lifecycle.state();
        if (lifecycle.stage() != CatacombsLifecycle.Stage.ACTIVE || state == null) return true;
        if (!ghosts.die(player.getUuid())) return true;
        state.recordDeath(false);
        stats.death(player.getUuid());
        applyGhost(player);
        broadcast(CatacombsMessages.died(player.getUsername()));
        player.sendMessage(CatacombsMessages.ghost(definition.rules().automaticGhostRevive()
                ? ghosts.secondsToRevive(player.getUuid()) : 0));
        if (ghosts.wiped(onlineIds())) lose("<c>Everyone in the party died!");
        return true;
    }

    public synchronized boolean revive(SkyBlockPlayer player) {
        if (!ghosts.revive(player.getUuid())) return false;
        releaseGhost(player);
        player.sendMessage(CatacombsMessages.revived());
        return true;
    }

    @Override
    public synchronized boolean reviveGhost(SkyBlockPlayer killer) {
        if (ghosts.isGhost(killer.getUuid())) return revive(killer);
        return onlineMembers().stream().filter(member -> ghosts.isGhost(member.getUuid())).findFirst().map(this::revive).orElse(false);
    }

    public synchronized int reviveAll() {
        int revived = 0;
        for (SkyBlockPlayer member : onlineMembers()) {
            if (revive(member)) revived++;
        }
        return revived;
    }

    private void applyGhost(SkyBlockPlayer player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);
        classes.onGhost(player);
    }

    private void releaseGhost(SkyBlockPlayer player) {
        classes.onRevive(player);
        player.setInvisible(false);
        player.setFlying(false);
        player.setAllowFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
    }

    public synchronized void recordSecret() {
        CatacombsRunState state = lifecycle.state();
        if (state != null) state.recordSecret();
    }

    public synchronized void recordCrypt() {
        CatacombsRunState state = lifecycle.state();
        if (state != null) state.recordCrypt();
    }

    private synchronized void tickSecond() {
        if (lifecycle.stage() == CatacombsLifecycle.Stage.WAITING) {
            if (readyCheck.tickSecond(onlineIds())) startCountdown();
            return;
        }
        CatacombsLifecycle.Tick tick = lifecycle.tick();
        if (lifecycle.stage() == CatacombsLifecycle.Stage.ACTIVE) {
            reviveDue();
            classes.tickSecond();
        }
        if (tick == CatacombsLifecycle.Tick.TIMED_OUT) {
            broadcast("<c>The dungeon ran out of time!");
            conclude(CatacombsLifecycle.Outcome.LOSE);
        } else if (tick == CatacombsLifecycle.Tick.SHUTDOWN) {
            sendEveryoneHome();
            later(TRANSFER_GRACE_TICKS, () -> CatacombsRuns.dispose(this));
        }
    }

    private void reviveDue() {
        for (UUID uuid : ghosts.tickSecond()) {
            onlineMembers().stream().filter(member -> member.getUuid().equals(uuid)).findFirst().ifPresent(member -> {
                releaseGhost(member);
                member.sendMessage(CatacombsMessages.revived());
            });
        }
    }

    private void poll() {
        DungeonEngine running = engine;
        if (running == null) return;
        if (lifecycle.stage() == CatacombsLifecycle.Stage.ENDING) {
            running.pollEnded(onlineMembers(), this::leaveToHub);
            return;
        }
        if (lifecycle.stage() != CatacombsLifecycle.Stage.ACTIVE) return;
        try {
            running.poll();
        } catch (Exception exception) {
            Logger.error(exception, "Catacombs run {} failed while polling", gameId);
        }
    }

    private void leaveToHub(SkyBlockPlayer player) {
        departed.add(player.getUuid());
        player.asProxyPlayer().transferToWithIndication(ServerType.SKYBLOCK_DUNGEON_HUB);
    }

    /** A member moving to another run on this server, after re-queueing. */
    public synchronized void depart(SkyBlockPlayer player) {
        departed.add(player.getUuid());
        CatacombsParticipant participant = players.get(player.getUuid());
        if (participant != null) handleDisconnect(participant);
        DungeonEngine running = engine;
        if (running != null) running.hideBossBars(player);
        if (onlineMembers().isEmpty() && emptySince == 0) emptySince = System.currentTimeMillis();
    }

    private void sendEveryoneHome() {
        for (Player player : arena.instance().getPlayers()) {
            if (player instanceof SkyBlockPlayer member) {
                member.asProxyPlayer().transferToWithIndication(ServerType.SKYBLOCK_DUNGEON_HUB);
            }
        }
    }

    @Override
    public void later(int ticks, Runnable action) {
        tasks.add(MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (lifecycle.stage() != CatacombsLifecycle.Stage.TERMINATED) action.run();
        }).delay(TaskSchedule.tick(Math.max(1, ticks))).schedule());
    }

    @Override
    public void broadcast(String message) {
        onlineMembers().forEach(player -> player.sendMessage(message));
    }

    @Override
    public void broadcast(Text message) {
        onlineMembers().forEach(player -> player.sendMessage(message));
    }

    public List<SkyBlockPlayer> onlineMembers() {
        return players.values().stream()
                .map(CatacombsParticipant::player)
                .filter(Player::isOnline)
                .filter(player -> player.getInstance() == arena.instance())
                .toList();
    }

    @Override
    public List<SkyBlockPlayer> alivePlayers() {
        return onlineMembers().stream().filter(player -> !ghosts.isGhost(player.getUuid())).toList();
    }

    private List<UUID> onlineIds() {
        return onlineMembers().stream().map(SkyBlockPlayer::getUuid).toList();
    }

    synchronized void shutDown() {
        if (!lifecycle.terminate()) return;
        tasks.forEach(Task::cancel);
        MobDeaths.forget(arena.instance());
        DungeonEngine running = engine;
        if (running != null) {
            running.dispose();
            running.disposeBoss();
        }
        for (Player player : arena.instance().getPlayers()) {
            player.setInstance(HypixelConst.getEmptyInstance(), new Pos(0.5, 100, 0.5));
        }
        super.dispose();
        MinecraftServer.getSchedulerManager().buildTask(arena::destroy).delay(TaskSchedule.seconds(2)).schedule();
    }

    public boolean isAbandoned(long now) {
        return CatacombsLifecycle.abandoned(onlineMembers().size(), emptySince, now, CatacombsRuns.EMPTY_LIFETIME_MILLIS);
    }

    public boolean isMember(UUID uuid) {
        return roster.contains(uuid);
    }

    public boolean isExpecting(UUID uuid) {
        CatacombsLifecycle.Stage stage = lifecycle.stage();
        return roster.contains(uuid) && !departed.contains(uuid)
                && (stage == CatacombsLifecycle.Stage.WAITING || stage == CatacombsLifecycle.Stage.ACTIVE);
    }

    public boolean isLeaver(UUID uuid) {
        return leavers.contains(uuid);
    }

    public UUID id() {
        return UUID.fromString(gameId);
    }

    public UUID creator() {
        return creator;
    }

    public long createdAt() {
        return createdAt;
    }

    public long seed() {
        return seed;
    }

    public CatacombsFloorId floorId() {
        return floorId;
    }

    public CatacombsFloorDefinition definition() {
        return definition;
    }

    public Set<UUID> roster() {
        return roster;
    }

    public List<UUID> disconnectedMembers() {
        return List.copyOf(disconnectedPlayers.keySet());
    }

    public CatacombsLifecycle lifecycle() {
        return lifecycle;
    }

    public CatacombsReadyCheck readyCheck() {
        return readyCheck;
    }

    public GhostRoster ghosts() {
        return ghosts;
    }

    public RunClasses classes() {
        return classes;
    }

    @Override
    public boolean isGhost(UUID player) {
        return ghosts.isGhost(player);
    }

    @Override
    public boolean classesActive() {
        return lifecycle.stage() == CatacombsLifecycle.Stage.ACTIVE;
    }

    @Override
    public boolean reviveAfter(UUID player, int seconds) {
        return ghosts.reviveAfter(player, seconds);
    }

    public @Nullable DungeonEngine engine() {
        return engine;
    }

    @Override
    public CatacombsArena arena() {
        return arena;
    }

    @Override
    public Instance instance() {
        return arena.instance();
    }

    @Override
    public CatacombsRunState state() {
        return lifecycle.state();
    }

    @Override
    public KeyRing keys() {
        return keys;
    }

    public DungeonClassType classOf(UUID player) {
        return classes.classOf(player);
    }

    public boolean started() {
        return lifecycle.stage() != CatacombsLifecycle.Stage.WAITING;
    }
}
