package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.ElleQuest;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Elle, the Crimson Isle guide. She follows the player's questline around the island (13 wiki spots).
public class NPCElle extends CrimsonNPC implements NPCAbiphoneTrait {
    public NPCElle() {
        super(CrimsonScene.ELLE, CrimsonSkins.ELLE, ElleQuest.STRONGHOLD);
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return ElleQuest.position(player);
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        ElleQuest.interact(this, player, false);
    }

    @Override
    public String getAbiphoneKey() {
        return "elle";
    }
}
