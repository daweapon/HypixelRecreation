package net.swofty.type.crystalhollows.npcs;

import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForge;

public class NPCForger extends AbstractStructureNPC {
    public NPCForger() {
        super("Forger", "<6>", DEFAULT_TEXTURE, "mithril_forger_room", "forger");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        skyBlock(event.player()).openView(new GUIForge());
    }
}
