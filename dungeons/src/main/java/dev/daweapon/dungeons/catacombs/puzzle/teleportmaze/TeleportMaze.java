package dev.daweapon.dungeons.catacombs.puzzle.teleportmaze;

import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.MazePad.Kind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * One run's Teleport Maze, as both captured F1 runs show it: the pads are joined in pairs (a pad sends you beside its
 * partner and the partner sends you back), the entry pad leads into one of the seven rooms, exactly one room pad leads
 * to the centre, and every teleport turns the player toward that final pad. The pairing is dealt fresh every run.
 */
public final class TeleportMaze {
    private static final int MAX_DEALS = 200;

    private final List<MazePad> pads;
    private final Map<MazePad, MazePad> partners;
    private final MazePad entry;
    private final MazePad centre;
    private final MazePad finalPad;

    private TeleportMaze(List<MazePad> pads, Map<MazePad, MazePad> partners, MazePad entry, MazePad centre, MazePad finalPad) {
        this.pads = List.copyOf(pads);
        this.partners = Map.copyOf(partners);
        this.entry = entry;
        this.centre = centre;
        this.finalPad = finalPad;
    }

    public static TeleportMaze deal(List<MazePad> pads, RandomGenerator random) {
        MazePad entry = only(pads, Kind.ENTRY);
        MazePad centre = only(pads, Kind.CENTRE);
        List<MazePad> rooms = pads.stream().filter(pad -> pad.kind() == Kind.ROOM).toList();
        if (rooms.size() % 2 != 0 || rooms.stream().map(MazePad::room).distinct().count() < 2) {
            throw new IllegalArgumentException("The Teleport Maze needs an even number of room pads in at least two rooms");
        }
        for (int attempt = 0; attempt < MAX_DEALS; attempt++) {
            TeleportMaze maze = tryDeal(pads, entry, centre, rooms, random);
            if (maze != null && maze.solvable()) return maze;
        }
        throw new IllegalStateException("Could not deal a solvable Teleport Maze");
    }

    private static MazePad only(List<MazePad> pads, Kind kind) {
        List<MazePad> found = pads.stream().filter(pad -> pad.kind() == kind).toList();
        if (found.size() != 1) throw new IllegalArgumentException("The Teleport Maze needs exactly one " + kind + " pad");
        return found.getFirst();
    }

    private static TeleportMaze tryDeal(List<MazePad> pads, MazePad entry, MazePad centre, List<MazePad> rooms, RandomGenerator random) {
        List<MazePad> left = new ArrayList<>(rooms);
        Collections.shuffle(left, Random.from(random));
        MazePad finalPad = left.removeFirst();
        MazePad first = left.stream().filter(pad -> pad.room() != finalPad.room()).findFirst().orElse(null);
        if (first == null) return null;
        left.remove(first);
        Map<MazePad, MazePad> partners = new HashMap<>();
        join(partners, finalPad, centre);
        join(partners, entry, first);
        while (!left.isEmpty()) {
            MazePad pad = left.removeFirst();
            MazePad other = left.stream().filter(candidate -> candidate.room() != pad.room()).findFirst().orElse(null);
            if (other == null) return null;
            left.remove(other);
            join(partners, pad, other);
        }
        return new TeleportMaze(pads, partners, entry, centre, finalPad);
    }

    private static void join(Map<MazePad, MazePad> partners, MazePad first, MazePad second) {
        partners.put(first, second);
        partners.put(second, first);
    }

    /** Whether the final pad's room can be reached from the room the entry pad leads to. */
    public boolean solvable() {
        int start = partner(entry).room();
        Set<Integer> seen = new HashSet<>(List.of(start));
        Deque<Integer> queue = new ArrayDeque<>(List.of(start));
        while (!queue.isEmpty()) {
            int room = queue.poll();
            if (room == finalPad.room()) return true;
            for (MazePad pad : pads) {
                if (pad.kind() != Kind.ROOM || pad.room() != room) continue;
                MazePad to = partner(pad);
                if (to.kind() == Kind.ROOM && seen.add(to.room())) queue.add(to.room());
            }
        }
        return false;
    }

    public MazePad partner(MazePad pad) {
        MazePad partner = partners.get(pad);
        if (partner == null) throw new IllegalArgumentException("Pad " + pad + " is not part of this maze");
        return partner;
    }

    public Optional<MazePad> padAt(int x, int z) {
        return pads.stream().filter(pad -> pad.at(x, z)).findFirst();
    }

    public List<MazePad> pads() {
        return pads;
    }

    public MazePad entry() {
        return entry;
    }

    public MazePad centre() {
        return centre;
    }

    public MazePad finalPad() {
        return finalPad;
    }

    /** The yaw from a landing spot toward the final pad's landing spot, as Hypixel turns the player. */
    public static float yawToward(double fromX, double fromZ, double toX, double toZ) {
        return (float) Math.toDegrees(Math.atan2(-(toX - fromX), toZ - fromZ));
    }
}
