package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForge;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCForger extends HypixelNPC {

    public NPCForger() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>The Forge", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(0.5, 149, -75.5, 0f, 0f);
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
        ((SkyBlockPlayer) event.player()).openView(new GUIForge());
    }
}
