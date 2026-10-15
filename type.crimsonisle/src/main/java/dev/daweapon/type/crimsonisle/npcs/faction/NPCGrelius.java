package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.ThroneGuardNPC;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;

public class NPCGrelius extends ThroneGuardNPC {
    public NPCGrelius() {
        super(CrimsonScene.GRELIUS, CrimsonSkins.GRELIUS, new Pos(-131.5, 101, -755.5, -90, 0), new Pos(-376.5, 117, -976.5));
    }
}
