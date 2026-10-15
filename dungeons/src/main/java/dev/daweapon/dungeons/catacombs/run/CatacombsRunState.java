package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.blessing.BlessingSet;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class CatacombsRunState {
    private final CatacombsRunConfig config;
    private final Clock clock;
    private final Instant startedAt;
    private final Map<Integer, DungeonRoomState> rooms = new HashMap<>();
    private final BlessingSet blessings = new BlessingSet();
    private DungeonRunPhase phase = DungeonRunPhase.START_ROOM;
    private int deaths;
    private int crypts;
    private int completedRooms;
    private int secretsFound;
    private int failedPuzzles;
    private boolean spiritPetFirstDeathReduction;
    private boolean mimicKilled;
    private boolean paulBonus;
    private boolean princeAttribute;
    private boolean batAttribute;
    private Instant finishedAt;

    private CatacombsRunState(CatacombsRunConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
        this.startedAt = clock.instant();
        for (int room = 0; room < config.totalRooms(); room++) {
            rooms.put(room, DungeonRoomState.UNDISCOVERED);
        }
    }

    public static CatacombsRunState start(CatacombsRunConfig config) {
        return start(config, Clock.systemUTC());
    }

    public static CatacombsRunState start(CatacombsRunConfig config, Clock clock) {
        return new CatacombsRunState(config, clock);
    }

    public CatacombsRunConfig config() {
        return config;
    }

    public DungeonRunPhase phase() {
        return phase;
    }

    public int deaths() {
        return deaths;
    }

    public int crypts() {
        return crypts;
    }

    public int completedRooms() {
        return completedRooms;
    }

    public int secretsFound() {
        return secretsFound;
    }

    public int failedPuzzles() {
        return failedPuzzles;
    }

    public Map<Integer, DungeonRoomState> rooms() {
        return Map.copyOf(rooms);
    }

    public BlessingSet blessings() {
        return blessings;
    }

    public Duration elapsed() {
        return Duration.between(startedAt, finishedAt == null ? clock.instant() : finishedAt);
    }

    public CatacombsRunState enterClearing() {
        phase = DungeonRunPhase.CLEARING;
        return this;
    }

    public CatacombsRunState enterBloodRoom() {
        phase = DungeonRunPhase.BLOOD_ROOM;
        return this;
    }

    public CatacombsRunState enterBoss() {
        phase = DungeonRunPhase.BOSS;
        return this;
    }

    public CatacombsRunState complete() {
        phase = DungeonRunPhase.COMPLETED;
        finishedAt = clock.instant();
        return this;
    }

    public CatacombsRunState fail() {
        phase = DungeonRunPhase.FAILED;
        finishedAt = clock.instant();
        return this;
    }

    public CatacombsRunState markRoom(int roomId, DungeonRoomState state) {
        DungeonRoomState previous = rooms.put(roomId, state);
        if (previous != DungeonRoomState.COMPLETED && state == DungeonRoomState.COMPLETED) {
            completedRooms++;
        }
        return this;
    }

    public CatacombsRunState recordDeath(boolean spiritPetReduced) {
        if (spiritPetReduced && !spiritPetFirstDeathReduction) {
            spiritPetFirstDeathReduction = true;
        }
        deaths++;
        return this;
    }

    public CatacombsRunState recordCrypt() {
        crypts++;
        return this;
    }

    public CatacombsRunState recordMimicKilled() {
        mimicKilled = true;
        return this;
    }

    public CatacombsRunState applyPaulBonus() {
        paulBonus = true;
        return this;
    }

    public CatacombsRunState triggerPrinceAttribute() {
        princeAttribute = true;
        return this;
    }

    public CatacombsRunState triggerBatAttribute() {
        batAttribute = true;
        return this;
    }

    public CatacombsRunState recordSecret() {
        secretsFound++;
        return this;
    }

    public CatacombsRunState failPuzzle(int roomId) {
        failedPuzzles++;
        markRoom(roomId, DungeonRoomState.FAILED);
        return this;
    }

    public CatacombsRunState addBlessing(BlessingType type, int level) {
        blessings.add(type, level);
        return this;
    }

    public DungeonScoreBreakdown score() {
        return DungeonScoreCalculator.calculate(scoreInput());
    }

    public DungeonScoreInput scoreInput() {
        return new DungeonScoreInput(
                config.floor().floor(),
                config.floor().mode(),
                deaths,
                spiritPetFirstDeathReduction,
                failedPuzzles,
                completedRooms,
                config.totalRooms(),
                secretsFound,
                config.totalSecrets(),
                elapsed().toSeconds(),
                crypts,
                mimicKilled,
                paulBonus,
                princeAttribute,
                batAttribute,
                phase != DungeonRunPhase.FAILED);
    }

    public Map<String, Integer> scoreTrace() {
        Map<String, Integer> trace = new HashMap<>();
        trace.put("deaths", deaths);
        trace.put("failedPuzzles", failedPuzzles);
        trace.put("completedRooms", completedRooms);
        trace.put("totalRooms", config.totalRooms());
        trace.put("secretsFound", secretsFound);
        trace.put("totalSecrets", config.totalSecrets());
        trace.put("crypts", crypts);
        trace.put("elapsedSeconds", (int) elapsed().toSeconds());
        trace.put("speedTimeLimitSeconds", config.floor().rules().speedTimeLimitSeconds());
        DungeonScoreBreakdown breakdown = score();
        trace.put("skill", breakdown.skill());
        trace.put("exploration", breakdown.exploration());
        trace.put("speed", breakdown.speed());
        trace.put("bonus", breakdown.bonus());
        trace.put("total", breakdown.total());
        return Map.copyOf(trace);
    }

    public Map<DungeonRunPhase, Boolean> progressionTrace() {
        Map<DungeonRunPhase, Boolean> trace = new EnumMap<>(DungeonRunPhase.class);
        for (DungeonRunPhase value : DungeonRunPhase.values()) {
            trace.put(value, hasReached(value));
        }
        return Map.copyOf(trace);
    }

    private boolean hasReached(DungeonRunPhase value) {
        if (phase == DungeonRunPhase.FAILED) {
            return value != DungeonRunPhase.COMPLETED;
        }
        return value.ordinal() <= phase.ordinal();
    }
}
