package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.forge.GUIForge;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class AbstractForger extends HypixelNPC {

    protected AbstractForger(Pos position) {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Forger", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.ARMORER;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (HotmService.data(player).getTier() < 2) {
            player.sendMessage("<e>[NPC] <b>Forger<f>: You need to unlock Heart of the Mountain Tier 2 to access the Forge!");
            return;
        }
        player.openView(new GUIForge());
    }
}
