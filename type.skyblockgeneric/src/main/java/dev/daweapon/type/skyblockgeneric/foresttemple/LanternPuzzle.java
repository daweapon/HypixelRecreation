package dev.daweapon.type.skyblockgeneric.foresttemple;

import java.util.List;

public final class LanternPuzzle {
    public enum Result { PROGRESS, SOLVED, WRONG }

    public record Lantern(int x, int y, int z, int pickles) {
        public boolean at(int bx, int by, int bz) {
            return x == bx && y == by && z == bz;
        }
    }

    public static final List<Lantern> SEQUENCE = List.of(
            new Lantern(-641, 56, 48, 0),
            new Lantern(-650, 59, 59, 1),
            new Lantern(-618, 66, 54, 2),
            new Lantern(-633, 73, 65, 3),
            new Lantern(-651, 69, 59, 4));

    private int progress;

    public static int indexOf(int x, int y, int z) {
        for (int i = 0; i < SEQUENCE.size(); i++) {
            if (SEQUENCE.get(i).at(x, y, z)) return i;
        }
        return -1;
    }

    public Result press(int lantern) {
        if (lantern == progress) {
            progress++;
            if (progress == SEQUENCE.size()) {
                progress = 0;
                return Result.SOLVED;
            }
            return Result.PROGRESS;
        }
        progress = lantern == 0 ? 1 : 0;
        return Result.WRONG;
    }

    public int progress() {
        return progress;
    }
}
