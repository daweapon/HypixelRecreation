package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public class GUIQuiver implements StatefulView<GUIQuiver.QuiverState> {
    private static final Map<CustomCollectionAward, Integer> SLOTS_PER_UPGRADE = new LinkedHashMap<>();
    private static final int STORAGE_SLOTS = 45;

    static {
        SLOTS_PER_UPGRADE.put(CustomCollectionAward.QUIVER, 27);
        SLOTS_PER_UPGRADE.put(CustomCollectionAward.QUIVER_UPGRADE_1, 9);
        SLOTS_PER_UPGRADE.put(CustomCollectionAward.QUIVER_UPGRADE_2, 9);
    }

    @Override
    public ViewConfiguration<QuiverState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.bags.quiver.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public QuiverState initialState() {
        return new QuiverState(0);
    }

    @Override
    public void layout(ViewLayout<QuiverState> layout, QuiverState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(50, ItemStacks.item(Material.LAVA_BUCKET, """
                <c>Clear Quiver
                <7>Deletes all arrows from your quiver."""), (_, c) -> {
            for (int i = 0; i < getUnlockedSlots((SkyBlockPlayer) c.player()); i++) c.inventory().setItemStack(i, ItemStack.AIR);
        });

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int amountOfSlots = getUnlockedSlots(player);
        int rawAmountOfSlots = 0;

        // Set up unlocked/locked slots
        for (Map.Entry<CustomCollectionAward, Integer> entry : SLOTS_PER_UPGRADE.entrySet()) {
            if (player.hasCustomCollectionAward(entry.getKey())) {
                rawAmountOfSlots += entry.getValue();
            } else {
                for (int i = 0; i < entry.getValue(); i++) {
                    int slotIndex = i + rawAmountOfSlots;
                    if (slotIndex < STORAGE_SLOTS) {
                        layout.slot(slotIndex, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                            Text.key("gui_sbmenu.bags.quiver.locked_slot"),
                            Text.keyLines("gui_sbmenu.bags.quiver.locked_slot.lore", entry.getKey().getDisplay())));
                    }
                }
                rawAmountOfSlots += entry.getValue();
            }
        }

        // Set up editable slots
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int i = 0; i < amountOfSlots; i++) {
            int slotIndex = i;
            SkyBlockItem item = quiver.getInSlot(slotIndex);

            layout.editable(slotIndex, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                if (item == null) {
                    return ItemStack.builder(Material.AIR);
                } else {
                    return PlayerItemUpdater.playerUpdate(p, item.getItemStack());
                }
            }, (slot, oldItem, newItem, s) -> {
                if (isItemAllowed(new SkyBlockItem(newItem))) return;
                ctx.inventory().setItemStack(slot, oldItem);
                ctx.player().getInventory().addItemStack(newItem);
                ctx.player().sendMessage(Text.key("gui_sbmenu.bags.quiver.msg.cannot_put"));
            });
        }
    }

    @Override
    public boolean onBottomClick(dev.daweapon.type.generic.gui.v2.context.ClickContext<QuiverState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (click.slot() < 0 || click.slot() >= player.getInventory().getSize()) return true;
        SkyBlockItem cursorItem = new SkyBlockItem(player.getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(player.getInventory().getItemStack(click.slot()));
        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) return true;
        player.sendMessage(Text.key("gui_sbmenu.bags.quiver.msg.cannot_put"));
        return false;
    }

    @Override
    public void onClose(QuiverState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        save(player, ctx.inventory(), getUnlockedSlots(player));
    }

    public static int getUnlockedSlots(SkyBlockPlayer player) {
        int amountOfSlots = 0;
        for (Map.Entry<CustomCollectionAward, Integer> entry : SLOTS_PER_UPGRADE.entrySet()) {
            if (player.hasCustomCollectionAward(entry.getKey())) {
                amountOfSlots += entry.getValue();
            }
        }
        return amountOfSlots;
    }

    static boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA()) return true;
        if (item.getMaterial().equals(Material.AIR)) return true;
        return item.getMaterial() == Material.ARROW;
    }

    private void save(SkyBlockPlayer player, net.minestom.server.inventory.Inventory inventory, int slotToSaveUpTo) {
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int i = 0; i < slotToSaveUpTo; i++) {
            SkyBlockItem item = new SkyBlockItem(inventory.getItemStack(i));
            if (item.isNA()) {
                quiver.getQuiverMap().remove(i);
            } else {
                quiver.getQuiverMap().put(i, item);
            }
        }
    }

    public record QuiverState(int placeholder) {}
}
