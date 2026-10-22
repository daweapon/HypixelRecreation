package dev.daweapon.type.skyblockgeneric.torrhus;

import java.util.List;

public final class ColorPuzzle {
    public enum Color { ORANGE, RED, BROWN, YELLOW, LIME, GREEN, PINK, MAGENTA, PURPLE, CYAN, LIGHT_BLUE, BLUE }

    public enum Result { PROGRESS, SOLVED, WRONG }

    public static final List<Color> SEQUENCE = List.of(Color.CYAN, Color.YELLOW, Color.RED, Color.BROWN, Color.ORANGE);

    private int progress;

    public Result press(Color color) {
        if (color == SEQUENCE.get(progress)) {
            progress++;
            if (progress == SEQUENCE.size()) {
                progress = 0;
                return Result.SOLVED;
            }
            return Result.PROGRESS;
        }
        progress = color == SEQUENCE.getFirst() ? 1 : 0;
        return Result.WRONG;
    }

    public int progress() {
        return progress;
    }
}
