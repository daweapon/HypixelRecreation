package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class GriffinBurrow {
    private final BlockVec position;
    private final BurrowKind kind;
    private final List<MythologicalCreatureMob> defenders = new ArrayList<>();
    @Setter
    private boolean opened;

    public GriffinBurrow(BlockVec position, BurrowKind kind) {
        this.position = position;
        this.kind = kind;
    }

    public boolean isAt(Point block) {
        return position.blockX() == block.blockX()
                && position.blockY() == block.blockY()
                && position.blockZ() == block.blockZ();
    }

    public boolean isDefended() {
        defenders.removeIf(defender -> defender.isDead() || defender.isRemoved());
        return !defenders.isEmpty();
    }
}
