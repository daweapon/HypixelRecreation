package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import net.minestom.server.coordinate.BlockVec;
import org.jetbrains.annotations.Nullable;

/**
 * A player's current chain of Griffin Burrows. The first burrow only points the way; every later
 * one holds either defenders or treasure.
 */
@Getter
public final class BurrowChain {
    private final int length;
    private int excavated;
    private GriffinBurrow current;
    private @Nullable BlockVec previous;

    public BurrowChain(int length, BlockVec start) {
        this.length = length;
        this.current = new GriffinBurrow(start, BurrowKind.START);
    }

    public boolean isLastBurrow() {
        return excavated + 1 >= length;
    }

    public void advance(BlockVec next, BurrowKind kind) {
        excavated++;
        previous = current.getPosition();
        current = new GriffinBurrow(next, kind);
    }

    public void finish() {
        excavated = length;
    }
}
