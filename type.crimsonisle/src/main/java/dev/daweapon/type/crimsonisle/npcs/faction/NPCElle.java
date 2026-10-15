package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.ElleQuest;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
