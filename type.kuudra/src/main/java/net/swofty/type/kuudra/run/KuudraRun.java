package net.swofty.type.kuudra.run;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.type.game.game.AbstractGame;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.event.PlayerPostJoinGameEvent;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.kuudra.arena.KuudraHollow;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraTierSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraPartyCheck;
import net.swofty.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraRun;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/** One party's Kuudra fight in its own copy of the Hollow. */
public final class KuudraRun extends AbstractGame<KuudraParticipant> {
    public static final int SHUTDOWN_SECONDS = 20;
    private static final int DIALOGUE_GAP_TICKS = 40;
    private static final int TRANSFER_GRACE_TICKS = 200;
    public static volatile KuudraWaveSpawner waveSpawner = KuudraWaveSpawner.NONE;

    private final KuudraTier tier;
    private final Set<UUID> roster;
    private final KuudraHollow hollow;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final KuudraRunState run;
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private volatile long emptySince = System.currentTimeMillis();
    private KuudraElle elle;

    KuudraRun(KuudraTier tier, List<UUID> roster, KuudraHollow hollow, KuudraConfig config) {
        super(hollow.instance(), _ -> {
        });
        if (roster.isEmpty() || roster.size() > KuudraPartyCheck.MAX_PARTY_SIZE) {
            throw new IllegalArgumentException("A Kuudra run needs 1 to " + KuudraPartyCheck.MAX_PARTY_SIZE + " players");
        }
        KuudraTierSpec spec = config.tier(tier);
        this.tier = tier;
        this.roster = Set.copyOf(roster);
        this.hollow = hollow;
        this.arena = config.arena();
        this.dialogue = config.dialogue();
        this.run = new KuudraRunState(spec.stunRequired(), spec.lairPhase(), spec.secondsPerWave(), config.maxWave(),
                SHUTDOWN_SECONDS);
    }

    void open() {
        elle = KuudraElle.spawnFor(this);
        tasks.add(MinecraftServer.getSchedulerManager().buildTask(this::tickSecond)
                .delay(TaskSchedule.seconds(1))
                .repeat(TaskSchedule.seconds(1))
                .schedule());
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
    public synchronized JoinResult join(KuudraParticipant participant) {
        UUID uuid = participant.getUuid();
        if (!roster.contains(uuid)) return new JoinResult.Denied("You aren't in this Kuudra party");
        if (players.containsKey(uuid)) return new JoinResult.Denied("Player is already in this game");
        if (hasDisconnectedPlayer(uuid)) {
            return handleRejoin(participant) ? new JoinResult.Success() : new JoinResult.Denied("Couldn't rejoin the run");
        }
        if (run.stage() != GameState.WAITING && run.stage() != GameState.IN_PROGRESS) {
            return new JoinResult.Denied("This Kuudra run is over");
        }
        players.put(uuid, participant);
        participant.setGameId(gameId);
        eventDispatcher.accept(new PlayerPostJoinGameEvent(this, participant.getServerPlayer(), players.size(), getMaxPlayers()));
        return new JoinResult.Success();
    }

    /** Brings an arriving party member into the Hollow: the bridge before the start, the platform after. */
    public boolean admit(SkyBlockPlayer player) {
        JoinResult result = join(new KuudraParticipant(player));
        if (result instanceof JoinResult.Denied(String reason)) {
            player.sendMessage("<c>" + reason + "!");
            return false;
        }
        emptySince = 0;
        boolean waiting = run.stage() == GameState.WAITING;
        Pos arrival = waiting ? arena.spawn() : arena.respawn();
        player.setRespawnPoint(arena.respawn());
        player.setInstance(hollow.instance(), arrival).thenRun(() -> {
            if (waiting) dialogue.chat("idle", ThreadLocalRandom.current()).forEach(player::sendMessage);
        });
        return true;
    }

    public synchronized void handleDisconnect(UUID uuid) {
        if (!roster.contains(uuid)) return;
        KuudraParticipant participant = players.get(uuid);
        if (participant != null) handleDisconnect(participant);
        if (onlineMembers().isEmpty() && emptySince == 0) emptySince = System.currentTimeMillis();
    }

    public void talkToElle(SkyBlockPlayer player) {
        if (!isMember(player.getUuid())) return;
        if (run.stage() != GameState.WAITING) return;
        begin();
    }

    /** Starts the fight as if someone talked to Elle. */
    public synchronized boolean begin() {
        if (!run.start()) return false;
        super.start();
        boolean veterans = onlineMembers().stream().allMatch(KuudraProgress::hasDefeatedKuudra);
        List<String> intro = dialogue.chat(veterans ? "start_veterans" : "start_first_time", ThreadLocalRandom.current());
        int gap = 0;
        for (String line : intro) {
            later(gap, () -> broadcast(line));
            gap += DIALOGUE_GAP_TICKS;
        }
        later(gap, this::collapseBridge);
        later(gap + DIALOGUE_GAP_TICKS, () -> say(KuudraPhase.SUPPLIES));
        spawnWave();
        return true;
    }

    public synchronized boolean advancePhase() {
        if (!run.advancePhase()) return false;
        say(run.phase());
        return true;
    }

    public synchronized boolean enterPhase(KuudraPhase phase) {
        if (!run.enterPhase(phase)) return false;
        say(phase);
        return true;
    }

    public synchronized boolean finish(KuudraRunState.Outcome outcome) {
        if (!run.finish(outcome)) return false;
        super.end();
        if (outcome == KuudraRunState.Outcome.WIN) {
            broadcast(dialogue.announcement("kuudra_down", Map.of()));
            dialogue.chat("defeated", ThreadLocalRandom.current()).forEach(this::broadcast);
            onlineMembers().forEach(this::reward);
        } else {
            broadcast(dialogue.announcement("defeat", Map.of()));
        }
        broadcast("<7>Returning to the Crimson Isle in <a>" + SHUTDOWN_SECONDS + " seconds<7>.");
        return true;
    }

    private void reward(SkyBlockPlayer player) {
        try {
            var chests = KuudraProgress.completeRun(player, tier);
            player.sendMessage("<7>Your chests are stored at <e>Vesuvius <7>for <a>72 hours<7>.");
            player.openView(new GUIKuudraRun(chests));
        } catch (Exception exception) {
            Logger.error(exception, "Couldn't pay {}'s Kuudra {} win", player.getUsername(), tier);
            player.sendMessage("<c>Something went wrong paying out your Kuudra rewards. Please report this to staff.");
        }
    }

    private synchronized void tickSecond() {
        KuudraRunState.Tick tick = run.tick();
        if (tick == KuudraRunState.Tick.NEW_WAVE) {
            spawnWave();
        } else if (tick == KuudraRunState.Tick.SHUTDOWN) {
            sendEveryoneHome();
            later(TRANSFER_GRACE_TICKS, () -> KuudraRuns.dispose(this));
        }
    }

    private void spawnWave() {
        try {
            waveSpawner.spawn(this, run.wave(), KuudraConfig.get().wave(run.wave()));
        } catch (Exception exception) {
            Logger.error(exception, "Kuudra wave {} failed to spawn", run.wave());
        }
    }

    private void sendEveryoneHome() {
        for (Player player : hollow.instance().getPlayers()) {
            if (player instanceof SkyBlockPlayer skyBlockPlayer) {
                skyBlockPlayer.asProxyPlayer().transferToWithIndication(ServerType.SKYBLOCK_CRIMSON_ISLE, arena.exit());
            }
        }
    }

    private void collapseBridge() {
        KuudraArena.Box bridge = arena.bridge();
        for (int x = bridge.minX(); x <= bridge.maxX(); x++) {
            for (int y = bridge.minY(); y <= bridge.maxY(); y++) {
                for (int z = bridge.minZ(); z <= bridge.maxZ(); z++) {
                    hollow.instance().setBlock(x, y, z, Block.AIR);
                }
            }
        }
        for (SkyBlockPlayer player : onlineMembers()) {
            if (player.getInstance() == hollow.instance() && !arena.onPlatform(player.getPosition())) {
                player.teleport(arena.respawn());
            }
        }
    }

    private void say(@Nullable KuudraPhase phase) {
        if (phase == null) return;
        String key = phase.dialogueKey(run.stunRequired());
        if (key != null) dialogue.chat(key, ThreadLocalRandom.current()).forEach(this::broadcast);
    }

    private void later(int ticks, Runnable action) {
        tasks.add(MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (run.stage() != GameState.TERMINATED) action.run();
        }).delay(TaskSchedule.tick(Math.max(1, ticks))).schedule());
    }

    public void broadcast(String message) {
        onlineMembers().forEach(player -> player.sendMessage(message));
    }

    public List<SkyBlockPlayer> onlineMembers() {
        return players.values().stream()
                .map(KuudraParticipant::player)
                .filter(Player::isOnline)
                .filter(player -> player.getInstance() == hollow.instance())
                .toList();
    }

    synchronized void shutDown() {
        if (!run.terminate()) return;
        tasks.forEach(Task::cancel);
        if (elle != null) elle.despawn();
        for (Player player : hollow.instance().getPlayers()) {
            player.setInstance(HypixelConst.getEmptyInstance(), new Pos(0.5, 100, 0.5));
        }
        super.dispose();
        MinecraftServer.getSchedulerManager().buildTask(hollow::destroy).delay(TaskSchedule.seconds(2)).schedule();
    }

    public boolean isAbandoned(long now) {
        return KuudraRunState.abandoned(onlineMembers().size(), emptySince, now, KuudraRuns.EMPTY_LIFETIME_MILLIS);
    }

    public boolean isMember(UUID uuid) {
        return roster.contains(uuid);
    }

    public boolean isExpecting(UUID uuid) {
        return roster.contains(uuid) && (run.stage() == GameState.WAITING || run.stage() == GameState.IN_PROGRESS);
    }

    public UUID id() {
        return UUID.fromString(gameId);
    }

    public KuudraTier tier() {
        return tier;
    }

    public Set<UUID> roster() {
        return roster;
    }

    public KuudraArena arena() {
        return arena;
    }

    public KuudraRunState state() {
        return run;
    }

    public List<UUID> disconnectedMembers() {
        return List.copyOf(disconnectedPlayers.keySet());
    }

    public boolean usesPlaceholderArena() {
        return hollow.placeholder();
    }
}
