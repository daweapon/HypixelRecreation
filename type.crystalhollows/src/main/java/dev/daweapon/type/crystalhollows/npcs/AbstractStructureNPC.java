package dev.daweapon.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;

public abstract class AbstractStructureNPC extends AbstractHollowsNPC {
    protected AbstractStructureNPC(String name, String color, String texture, String structureId, String anchorName) {
        super(name, color, texture, () -> CrystalHollowsLayout.anchor(structureId, anchorName));
    }

    protected AbstractStructureNPC(String name, String color, String texture, String structureId, String anchorName, Pos fallback) {
        super(name, color, texture, () -> {
            Pos anchor = CrystalHollowsLayout.anchor(structureId, anchorName);
            return anchor == null ? fallback : anchor;
        });
    }
}
