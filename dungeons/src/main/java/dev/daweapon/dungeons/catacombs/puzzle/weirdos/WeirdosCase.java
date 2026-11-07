package dev.daweapon.dungeons.catacombs.puzzle.weirdos;

import java.util.List;

/**
 * One of the six statement sets: what the first, second and third weirdo say, with {@code {1}}, {@code {2}} and
 * {@code {3}} standing for their names, and which of them ({@code 0..2}) has the reward in their chest.
 */
public record WeirdosCase(int rewarded, List<String> lines) {
    public static final int WEIRDOS = 3;

    public WeirdosCase {
        lines = List.copyOf(lines);
        if (lines.size() != WEIRDOS || rewarded < 0 || rewarded >= WEIRDOS) {
            throw new IllegalArgumentException("A Three Weirdos case needs three lines and a rewarded weirdo");
        }
    }
}
