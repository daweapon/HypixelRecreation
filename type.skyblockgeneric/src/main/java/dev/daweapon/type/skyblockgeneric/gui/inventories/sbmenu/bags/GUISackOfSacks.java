package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.RawClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSackOfSacks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SackComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GUISackOfSacks implements StatefulView<GUISackOfSacks.SackOfSacksState> {
    private static final SortedMap<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = new TreeMap<>(Map.of(
            CustomCollectionAward.SACK_OF_SACKS, 8,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_1, 12,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_2, 16,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_3, 20,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_4, 24,
            CustomCollectionAward.SACK_OF_SACKS_UPGRADE_5, 29
    ));

    @Override
    public ViewConfiguration<SackOfSacksState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.bags.sack_of_sacks.title", InventoryType.CHEST_5_ROW);
    }

    @Override
    public SackOfSacksState initialState() {
        return new SackOfSacksState(0);
    }

    @Override
    public void layout(ViewLayout<SackOfSacksState> layout, SackOfSacksState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int totalSlots = getTotalSlots(player);
        int slotsPerPage = 45;

        for (int i = 0; i < totalSlots; i++) {
            SkyBlockItem item = player.getSackOfSacks().getInSlot(i);
            layout.editable(i, (s, c) -> item == null
                    ? ItemStack.builder(Material.AIR)
                    : PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), item.getItemStack()), (slot, oldItem, newItem, s) -> {
            });
        }

        // Locked slots
        for (int i = totalSlots; i < slotsPerPage; i++) {
            CustomCollectionAward nextUpgrade = getUpgradeNeededForSlotIndex(i);
            if (nextUpgrade != null) {
                layout.slot(i, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                    Text.key("gui_sbmenu.bags.sack_of_sacks.locked_slot"),
                    Text.keyLines("gui_sbmenu.bags.sack_of_sacks.locked_slot.lore", nextUpgrade.getDisplay())));
            }
        }

        // Insert inventory button
        layout.slot(38, (s, c) -> ItemStacks.item(Material.CHEST, 1,
                        Text.key("gui_sbmenu.bags.sack_of_sacks.insert_inventory"),
                        Text.keyLines("gui_sbmenu.bags.sack_of_sacks.insert_inventory.lore")),
                (click, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    int slot = 0;
                    for (ItemStack itemStack : p.getInventory().getItemStacks()) {
                        SkyBlockItem sItem = new SkyBlockItem(itemStack);
                        ItemType type = sItem.getAttributeHandler().getPotentialType();
                        if (p.canInsertItemIntoSacks(type)) {
                            p.getSackItems().increase(type, sItem.getAmount());
                            p.getInventory().setItemStack(slot, ItemStack.AIR);
                        }
                        slot++;
                    }
                });
    }

    @Override
    public void onClose(SackOfSacksState state, ViewContext ctx, ViewSession.CloseReason reason) {
        save((SkyBlockPlayer) ctx.player(), ctx);
    }

    @Override
    public boolean onRawClick(RawClickContext<SackOfSacksState> click, ViewContext ctx) {
        if (!click.inViewInventory() || !(click.click() instanceof Click.Right)) return false;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int slot = click.slot();
        if (slot < 0 || slot >= getTotalSlots(player) || !player.getInventory().getCursorItem().isAir()) return false;
        SkyBlockItem sack = new SkyBlockItem(ctx.inventory().getItemStack(slot));
        if (sack.isNA() || sack.isAir() || !sack.hasComponent(SackComponent.class)) return false;
        click.cancel();
        ctx.push(new GUISack(sack.getAttributeHandler().getPotentialType(), true));
        return true;
    }

    @Override
    public boolean onBottomClick(ClickContext<SackOfSacksState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (click.slot() < 0 || click.slot() >= player.getInventory().getSize()) return true;
        SkyBlockItem cursorItem = new SkyBlockItem(player.getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(player.getInventory().getItemStack(click.slot()));

        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) {
            return true;
        }

        player.sendMessage(Text.key("gui_sbmenu.bags.sack_of_sacks.msg.cannot_put"));
        return false;
    }

    public static int getTotalSlots(SkyBlockPlayer player) {
        int totalSlots = 0;
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (player.hasCustomCollectionAward(entry)) {
                totalSlots = Math.max(totalSlots, SLOTS_PER_UPGRADE.get(entry));
            }
        }
        return totalSlots;
    }

    private CustomCollectionAward getUpgradeNeededForSlotIndex(int slotIndex) {
        for (CustomCollectionAward entry : SLOTS_PER_UPGRADE.keySet()) {
            if (slotIndex < SLOTS_PER_UPGRADE.get(entry)) {
                return entry;
            }
        }
        return null;
    }

    private void save(SkyBlockPlayer player, ViewContext ctx) {
        DatapointSackOfSacks.PlayerSackOfSacks sackOfSacks = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SACK_OF_SACKS, DatapointSackOfSacks.class).getValue();
        int totalSlots = getTotalSlots(player);

        for (int i = 0; i < totalSlots; i++) {
            SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(i));
            if (item.isNA() || item.getMaterial() == Material.AIR) {
                sackOfSacks.removeFromSlot(i);
            } else {
                sackOfSacks.setInSlot(i, item);
            }
        }
    }

    private boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;
        return item.hasComponent(SackComponent.class);
    }

    public record SackOfSacksState(int placeholder) {}
}
