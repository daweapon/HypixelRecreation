package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.ThroneGuardNPC;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;

public class NPCGrelius extends ThroneGuardNPC {
    public NPCGrelius() {
        super(CrimsonScene.GRELIUS, CrimsonSkins.GRELIUS, new Pos(-131.5, 101, -755.5, -90, 0), new Pos(-376.5, 117, -976.5));
    }
}
