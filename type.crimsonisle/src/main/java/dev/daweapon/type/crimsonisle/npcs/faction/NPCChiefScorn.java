package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.BarbarianScenes;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Chief Scorn, leader of the Barbarian Faction, on his throne in the Chief's Hut.
public class NPCChiefScorn extends CrimsonNPC {
    public NPCChiefScorn() {
        super(CrimsonScene.CHIEF_SCORN, CrimsonSkins.CHIEF_SCORN, new Pos(-580.5, 115.5, -687.5, 90, 0));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        switch (CrimsonQuestlines.barbarian(player)) {
            case ESCORT_UGO -> BarbarianScenes.chiefWelcome(player);
            case PRESENT_RESEARCH -> BarbarianScenes.presentResearch(player);
            case TALK_TO_CHIEF -> BarbarianScenes.sulphurStolen(player);
            case MEET_AT_FORGOTTEN_SKULL, DEFEAT_KUUDRA -> say(player, "<f><l>Please help Ugo.");
            case TALK_TO_CHIEF_FINAL -> BarbarianScenes.afterKuudra(player);
            default -> sayRandom(player, "<f><l>I have an important meeting to attend.", "<f><l>I am very busy right now.");
        }
    }
}
