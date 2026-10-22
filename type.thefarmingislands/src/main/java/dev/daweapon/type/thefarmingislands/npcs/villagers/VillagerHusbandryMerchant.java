package dev.daweapon.type.thefarmingislands.npcs.villagers;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;

import java.util.List;

public class VillagerHusbandryMerchant extends HypixelNPC {
    private static final String MET = "npc.husbandry_merchant";

    public VillagerHusbandryMerchant() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Husbandry Merchant", "<e><l>CLICK"};
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<e>Husbandry Merchant";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(116.5, 73, -255.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.BUTCHER;
            }
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("hello").lines(
                        "Are you in the market for a Husbandry Axe?",
                        "They'll help you gather drops from animals faster!"
                ).build()
        ).toArray(DialogueSet[]::new);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        if (!ChapterTriggers.done(player, MET)) {
            setDialogue(player, "hello").thenRun(() -> {
                ChapterTriggers.fire(player, MET);
                player.openView(new CatalogueShopView("husbandry_merchant"));
            });
            return;
        }
        player.openView(new CatalogueShopView("husbandry_merchant"));
    }
}
