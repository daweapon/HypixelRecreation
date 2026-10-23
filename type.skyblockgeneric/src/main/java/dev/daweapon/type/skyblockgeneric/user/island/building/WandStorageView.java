package dev.daweapon.type.skyblockgeneric.user.island.building;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeBackpackData;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class WandStorageView extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Builder's Wand Storage", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem wand = heldWand(player);
        for (int slot = 0; slot < 9; slot++) {
            layout.slot(slot, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        }
        Components.close(layout, 4);
        List<UnderstandableSkyBlockItem> stored = wand == null ? List.of() : wand.getAttributeHandler().getBackpackData().items();
        for (int index = 0; index < WandStorage.SLOTS; index++) {
            UnderstandableSkyBlockItem content = index < stored.size() ? stored.get(index) : null;
            layout.editable(index + 9, (_, c) -> {
                if (content == null || new SkyBlockItem(content).isNA() || new SkyBlockItem(content).isAir()) return ItemStack.builder(Material.AIR);
                return PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), new SkyBlockItem(content).getItemStack());
            }, (_, _, _, _) -> {
            });
        }
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem wand = heldWand(player);
        if (wand == null) return;
        List<UnderstandableSkyBlockItem> contents = new ArrayList<>();
        for (int slot = 9; slot < WandStorage.SLOTS + 9; slot++) {
            SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(slot));
            contents.add(item.getAttributeHandler().getPotentialType() == ItemType.BUILDERS_WAND || item.isNA() ? new SkyBlockItem(ItemType.AIR).toUnderstandable() : item.toUnderstandable());
        }
        wand.getAttributeHandler().setBackpackData(new ItemAttributeBackpackData.BackpackData(contents));
        player.setItemInMainHand(PlayerItemUpdater.playerUpdate(player, wand.getItemStack()).build());
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }

    private static SkyBlockItem heldWand(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        return held.getAttributeHandler().getPotentialType() == ItemType.BUILDERS_WAND ? held : null;
    }
}
