package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.npc.GardenNpcSkins;
import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUIFeast;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
