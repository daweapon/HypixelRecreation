package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCPearlDealer extends AbstractEndNPC {
    private static final String MET = "the_end:pearl_dealer:met";

    public NPCPearlDealer() {
        super(null, "Pearl Dealer", "<9>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2EyYTRlZGU4ZWU5YzYyZTJlMTQ4ZTE1ZTdkNmViN2EzNDliOTA2OTdlN2UyNmRlNWY1M2I5NzY0ZjMzNWFlZCJ9fX0=",
                new Pos(-504.5, 101, -284.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (LoneAdventurerProgress.stage(player) == LoneAdventurerStage.PEARL_DEALER_REWARD) {
            setDialogue(player, "shortbow").thenRun(() -> {
                player.addAndUpdateItem(ItemType.DRAGON_SHORTBOW);
                LoneAdventurerProgress.receiveShortbow(player);
            });
            return;
        }
        if (DatapointCrimsonIsle.of(player).getNumber(MET) == 0) {
            setDialogue(player, "welcome").thenRun(() -> {
                DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(MET, 1L));
                player.openView(new CatalogueShopView("pearl_dealer"));
            });
            return;
        }
        player.openView(new CatalogueShopView("pearl_dealer"));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("welcome").lines(
                        "Welcome to <5>The End<f>, traveller!",
                        "I deal in <d>pearls<f> and everything an adventurer needs to take on the <5>Ender Dragon<f>.",
                        "Have a look at what I have!"
                ).build(),
                DialogueSet.builder().key("shortbow").lines(
                        "The <b>Lone Adventurer <f>sent you? He told me you brought him a <5>Dragon Shortbow<f>.",
                        "Then you have earned one of your own. Take it, free of charge!"
                ).build()
        };
    }
}
