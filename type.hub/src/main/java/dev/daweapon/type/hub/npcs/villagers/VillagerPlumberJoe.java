package dev.daweapon.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;

public class VillagerPlumberJoe extends HypixelNPC {
    public VillagerPlumberJoe() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Plumber Joe", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(123.5, 74, -38.5, -75, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.TOOLSMITH;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        HypixelPlayer player = e.player();
        if (!isInDialogue(player)) setDialogue(player, "hello");
        player.openView(new CatalogueShopView("plumber_joe"));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("hello", "npcs_hub.plumber_joe.dialogue.hello")
        };
    }
}
