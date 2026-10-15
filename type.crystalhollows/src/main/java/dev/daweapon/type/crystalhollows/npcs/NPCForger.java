package dev.daweapon.type.crystalhollows.npcs;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.forge.GUIForge;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCForger extends AbstractStructureNPC {
    private static final int REQUIRED_HOTM_TIER = 2;

    public NPCForger() {
        super("Forger", "<b>", DEFAULT_TEXTURE, "mithril_forger_room", "forger");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (HotmService.data(player).getTier() < REQUIRED_HOTM_TIER) {
            sendNPCMessage(player, "You need to unlock Heart of the Mountain Tier " + REQUIRED_HOTM_TIER + " to access the Forge!");
            return;
        }
        player.openView(new GUIForge());
    }
}
