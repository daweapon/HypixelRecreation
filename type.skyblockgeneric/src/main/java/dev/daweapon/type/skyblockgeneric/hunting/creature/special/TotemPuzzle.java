package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import java.util.Random;

final class TotemPuzzle {
    static final int HEADS = 3;
    static final int DIRECTIONS = 4;

    private final int[] facing;

    TotemPuzzle(int bottom, int middle, int top) {
        this.facing = new int[]{bottom, middle, top};
    }

    static TotemPuzzle random(Random random) {
        while (true) {
            TotemPuzzle puzzle = new TotemPuzzle(random.nextInt(DIRECTIONS), random.nextInt(DIRECTIONS), random.nextInt(DIRECTIONS));
            if (!puzzle.topLocked()) return puzzle;
        }
    }

    static int partner(int head) {
        return head == HEADS - 1 ? 0 : head + 1;
    }

    static int headAt(double heightAboveBase, double headHeight) {
        int index = (int) Math.floor(heightAboveBase / headHeight);
        return Math.max(0, Math.min(HEADS - 1, index));
    }

    int facing(int head) {
        return facing[head];
    }

    boolean topLocked() {
        return facing[1] == facing[2];
    }

    boolean awake() {
        return facing[0] == facing[1] && facing[1] == facing[2];
    }

    boolean[] click(int head, boolean clockwise) {
        boolean locked = topLocked();
        boolean[] rotated = new boolean[HEADS];
        int step = clockwise ? 1 : DIRECTIONS - 1;
        for (int index : new int[]{head, partner(head)}) {
            if (locked && index >= 1) continue;
            facing[index] = (facing[index] + step) % DIRECTIONS;
            rotated[index] = true;
        }
        return rotated;
    }
}
