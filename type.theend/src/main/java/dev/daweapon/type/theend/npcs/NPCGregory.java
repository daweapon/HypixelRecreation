package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.dragon.EnderDragonFight;

public class NPCGregory extends AbstractEndNPC {
    public NPCGregory() {
        super(null, "Gregory", "<9>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyMmQ4ZTc1MWM4ZjJmZDRjODk0MmM0NGJkYjJmNWNhNGQ4YWU4ZTU3NWVkM2ViMzRjMThhODZlOTNiIn19fQ==",
                new Pos(-607.5, 22, -284.5, 0, 0), _ -> EnderDragonFight.isActive());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player) || !EnderDragonFight.isActive()) return;
        setDialogue(player, "hello").thenRun(() -> player.openView(new CatalogueShopView("gregory")));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Hey, I'm an opportunist!",
                        "I sell Arrows and Bows for a truly overpriced amount!"
                ).build()
        };
    }
}
