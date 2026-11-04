package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public final class RelayPuzzle {
    public static final int COLUMNS = 4;
    public static final int ROWS = 4;
    public static final long BOMB_FUSE_MILLIS = 3000;
    public static final long BOMB_ROLL_MILLIS = 4000;
    public static final long MAX_DISABLE_MILLIS = 3000;
    private static final List<String> SOUNDS = List.of(
            "entity.firework_rocket.blast", "entity.zombie.ambient", "entity.cat.ambient", "entity.villager.ambient",
            "entity.cow.ambient", "block.note_block.harp", "block.note_block.bell", "entity.experience_orb.pickup");
    private static final float[] PITCHES = {0.5f, 0.7f, 0.9f, 1.1f, 1.3f, 1.5f, 1.7f, 1.9f};

    public enum Outcome {
        NOTHING, CORRECT, COLUMN_DONE, WRONG, SOLVED, EXPLODED, TIMED_OUT
    }

    public record Tone(String sound, float pitch) {
    }

    private final NineFRelay relay;
    private final Tone[][] tones;
    private final boolean[][] locked = new boolean[COLUMNS][ROWS];
    private final long deadline;
    private int column;
    private long disabledUntil;
    private int bombSlot = -1;
    private long bombExplodesAt;
    private long nextBombRoll;
    private boolean over;

    private RelayPuzzle(NineFRelay relay, Tone[][] tones, long now) {
        this.relay = relay;
        this.tones = tones;
        this.deadline = now + relay.timerSeconds() * 1000L;
    }

    public static RelayPuzzle create(NineFRelay relay, long now, RandomGenerator random) {
        Tone[][] tones = new Tone[COLUMNS][ROWS];
        for (int column = 0; column < COLUMNS; column++) {
            String sound = SOUNDS.get(random.nextInt(SOUNDS.size()));
            List<Float> pitches = new ArrayList<>();
            for (float pitch : PITCHES) pitches.add(pitch);
            Collections.shuffle(pitches, new java.util.Random(random.nextLong()));
            for (int row = 0; row < ROWS; row++) tones[column][row] = new Tone(sound, pitches.get(row));
        }
        return new RelayPuzzle(relay, tones, now);
    }

    public static int tileSlot(int column, int row) {
        return (row + 1) * 9 + 1 + column * 2;
    }

    public static int gapSlot(int gap, int row) {
        return (row + 1) * 9 + 2 + gap * 2;
    }

    public NineFRelay relay() {
        return relay;
    }

    public int column() {
        return column;
    }

    public boolean isOver() {
        return over;
    }

    public Tone tone(int column, int row) {
        return tones[column][row];
    }

    public boolean isLocked(int column, int row) {
        return locked[column][row];
    }

    public boolean isDisabled(long now) {
        return now < disabledUntil;
    }

    public int bombSlot() {
        return bombSlot;
    }

    public long bombExplodesAt() {
        return bombExplodesAt;
    }

    public int secondsLeft(long now) {
        return (int) Math.max(0, (deadline - now + 999) / 1000);
    }

    public Outcome lockIn(int column, int row, long now, RandomGenerator random) {
        if (over || column != this.column || locked[column][row] || isDisabled(now)) return Outcome.NOTHING;
        float lowest = Float.MAX_VALUE;
        for (int other = 0; other < ROWS; other++) {
            if (!locked[column][other]) lowest = Math.min(lowest, tones[column][other].pitch());
        }
        if (tones[column][row].pitch() > lowest) {
            for (int other = 0; other < ROWS; other++) locked[column][other] = false;
            disabledUntil = now + (long) (random.nextDouble() * MAX_DISABLE_MILLIS);
            return Outcome.WRONG;
        }
        locked[column][row] = true;
        for (int other = 0; other < ROWS; other++) if (!locked[column][other]) return Outcome.CORRECT;
        this.column++;
        if (this.column >= COLUMNS) {
            over = true;
            return Outcome.SOLVED;
        }
        if (this.column == 1) nextBombRoll = now + BOMB_ROLL_MILLIS;
        return Outcome.COLUMN_DONE;
    }

    public boolean disarm(int slot) {
        if (bombSlot < 0 || slot != bombSlot) return false;
        bombSlot = -1;
        return true;
    }

    public Outcome tick(long now, RandomGenerator random) {
        if (over) return Outcome.NOTHING;
        if (bombSlot >= 0 && now >= bombExplodesAt) {
            over = true;
            return Outcome.EXPLODED;
        }
        if (now >= deadline) {
            over = true;
            return Outcome.TIMED_OUT;
        }
        if (column >= 1 && bombSlot < 0 && relay.bombChance() > 0 && now >= nextBombRoll) {
            nextBombRoll = now + BOMB_ROLL_MILLIS;
            if (random.nextDouble() < relay.bombChance()) {
                bombSlot = gapSlot(random.nextInt(COLUMNS - 1), random.nextInt(ROWS));
                bombExplodesAt = now + BOMB_FUSE_MILLIS;
            }
        }
        return Outcome.NOTHING;
    }
}
