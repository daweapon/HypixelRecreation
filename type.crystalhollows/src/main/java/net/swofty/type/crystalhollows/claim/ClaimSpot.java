package net.swofty.type.crystalhollows.claim;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.crystalhollows.config.Box;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;

import java.util.List;

public record ClaimSpot(NucleusCrystal crystal, String structure, Pos crystalAt, Box cube, List<Box> walls) {

    public Pos crystalPosition() {
        Pos origin = CrystalHollowsLayout.origin(structure);
        return origin == null ? null : origin.add(crystalAt.x(), crystalAt.y(), crystalAt.z());
    }

    public boolean containsBlock(Point block) {
        Pos origin = CrystalHollowsLayout.origin(structure);
        return origin != null && cube.shift(origin).contains(block.add(0.5, 0.5, 0.5));
    }

    public List<Box> wallsAt(Pos origin) {
        return walls.stream().map(wall -> wall.shift(origin)).toList();
    }
}
