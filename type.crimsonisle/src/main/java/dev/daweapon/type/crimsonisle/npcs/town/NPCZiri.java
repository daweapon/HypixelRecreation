package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.gui.town.GUIZiri;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Ziri in the Scarleton Bank, who restores removed Crimson Isle quest contacts (wiki Ziri; moved Aug 2025). */
public class NPCZiri extends CrimsonNPC {
    public NPCZiri() {
        super("<e>Ziri", TownSkins.ZIRI, new Pos(-74.5, 107, -793.5));
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        say(player, "I know people!",
                "If you ever removed an <3>Abiphone Contact <f>by accident and don't know how to reach them, maybe I can hook you up!")
                .thenRun(() -> player.openView(new GUIZiri()));
    }
}
