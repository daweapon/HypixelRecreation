package dev.daweapon.type.crimsonisle.npcs.faction;

import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.ElleQuest;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// While the player explores both towns, Elle also waits in front of the Chief's Hut in Dragontail.
public class NPCElleDragontail extends CrimsonNPC {
    public NPCElleDragontail() {
        super(CrimsonScene.ELLE, CrimsonSkins.ELLE, ElleQuest.DRAGONTAIL);
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        return ElleQuest.showsInDragontail(player);
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        ElleQuest.interact(this, player, true);
    }

    @Override
    public String getName() {
        return "Elle (Dragontail)";
    }
}
