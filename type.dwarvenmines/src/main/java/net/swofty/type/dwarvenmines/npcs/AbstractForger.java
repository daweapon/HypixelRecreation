package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.forge.GUIForge;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
        ((SkyBlockPlayer) event.player()).openView(new GUIForge());
    }
}
