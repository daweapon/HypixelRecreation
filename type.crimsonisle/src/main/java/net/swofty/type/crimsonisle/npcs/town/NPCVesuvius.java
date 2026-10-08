package net.swofty.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.gui.town.GUIVesuvius;
import net.swofty.type.crimsonisle.town.TownSkins;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Vesuvius at the Forgotten Skull (added Oct 2025), who keeps unopened Kuudra reward chests for 72
 * hours. Stands on the wool at y 114; the wiki's y 115 floats a block up.
 */
public class NPCVesuvius extends CrimsonNPC {
    public NPCVesuvius() {
        super("<e>Vesuvius", TownSkins.VESUVIUS, new Pos(-380.5, 114, -1030.5));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonSideQuests.firstMeeting(player, "vesuvius")) {
            say(player, "<c>Kuudra <f>runs are full of goodies.", "If you don't have time to claim them, I got your back.")
                    .thenRun(() -> player.openView(new GUIVesuvius()));
            return;
        }
        player.openView(new GUIVesuvius());
    }
}
