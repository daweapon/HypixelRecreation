package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIFeast;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCFeastChefTed extends HypixelNPC {

    public NPCFeastChefTed() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Feast Chef Ted", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return GardenNpcSkins.of("FEAST_CHEF_TED").signature();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return GardenNpcSkins.of("FEAST_CHEF_TED").texture();
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(84.5, 72, -78.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        player.openView(new GUIFeast());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[0];
    }
}
