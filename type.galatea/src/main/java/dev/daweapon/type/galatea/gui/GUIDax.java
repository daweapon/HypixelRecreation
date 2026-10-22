package dev.daweapon.type.galatea.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

public class GUIDax extends StatelessView {
    public static final String CLAIMED_TRIGGER = "npc.dax.huntrap";
    public static final String SMALL_HUNTRAP = "RETIA_BASICA";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Small Huntrap", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 22);
        ItemType huntrap = ItemType.get(SMALL_HUNTRAP);
        if (huntrap == null) return;
        layout.slot(13, (_, c) -> ItemStacks.appendLore(
                PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), new SkyBlockItem(huntrap).getItemStack()),
                "\n<e>Click to claim!"), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (ChapterTriggers.done(player, CLAIMED_TRIGGER)) {
                player.sendMessage("<c>You already claimed a Small Huntrap!");
                return;
            }
            ChapterTriggers.fire(player, CLAIMED_TRIGGER);
            player.addAndUpdateItem(huntrap);
            player.sendMessage("<a>You claimed a <f>Small Huntrap<a>!");
            player.closeInventory();
        });
    }
}
