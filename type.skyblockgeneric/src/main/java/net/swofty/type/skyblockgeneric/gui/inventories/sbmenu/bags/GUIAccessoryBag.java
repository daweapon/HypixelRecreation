package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBag;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBagPages;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBagSlots;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * The Accessory Bag, 45 slots a page. Turning a page saves the open page and reopens the view on the
 * next one, so every page's slots are loaded fresh from the bag.
 */
public class GUIAccessoryBag implements StatefulView<GUIAccessoryBag.AccessoryBagState> {
    public static final int SLOTS_PER_JACOBUS_UPGRADE = AccessoryBagSlots.PER_JACOBUS_UPGRADE;
    public static final int MAX_JACOBUS_UPGRADES = AccessoryBagSlots.MAX_JACOBUS_UPGRADES;
    public static final int SKYBLOCK_XP_PER_JACOBUS_UPGRADE = 2;

    @Override
    public ViewConfiguration<AccessoryBagState> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> Text.key("gui_sbmenu.bags.accessory.title", state.page() + 1,
                        AccessoryBagPages.pages(AccessoryBagSlots.total((SkyBlockPlayer) ctx.player()))),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    public AccessoryBagState initialState() {
        return new AccessoryBagState(0);
    }

    @Override
    public void layout(ViewLayout<AccessoryBagState> layout, AccessoryBagState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int page = state.page();
        int totalSlots = AccessoryBagSlots.total(player);
        int totalPages = AccessoryBagPages.pages(totalSlots);
        int startIndex = AccessoryBagPages.firstSlot(page);
        int openSlots = AccessoryBagPages.slotsOnPage(page, totalSlots);

        for (int i = 0; i < openSlots; i++) {
            SkyBlockItem item = player.getAccessoryBag().getInSlot(i + startIndex);
            layout.editable(i, (s, c) -> item == null
                    ? ItemStack.builder(Material.AIR)
                    : PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), item.getItemStack()),
                    (slot, oldItem, newItem, s) -> {
                    });
        }

        CustomCollectionAward nextUpgrade = AccessoryBagSlots.nextRedstoneUpgrade(player);
        for (int i = openSlots; i < AccessoryBagPages.SLOTS_PER_PAGE; i++) {
            if (nextUpgrade == null) break;
            layout.slot(i, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                    Text.key("gui_sbmenu.bags.accessory.locked"),
                    Text.keyLines("gui_sbmenu.bags.accessory.locked.lore", nextUpgrade.getDisplay())));
        }

        if (page > 0) {
            layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.bags.accessory.previous_page"), List.of()),
                    (click, c) -> c.replace(new GUIAccessoryBag(), new AccessoryBagState(page - 1)));
        }
        if (page < totalPages - 1) {
            layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.bags.accessory.next_page"), List.of()),
                    (click, c) -> c.replace(new GUIAccessoryBag(), new AccessoryBagState(page + 1)));
        }
    }

    @Override
    public void onClose(AccessoryBagState state, ViewContext ctx, ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        AccessoryBagPages.writePage(bag, state.page(), AccessoryBagSlots.total(player),
                slot -> new SkyBlockItem(ctx.inventory().getItemStack(slot)));
        AccessoryBag.save(player, bag);
    }

    @Override
    public boolean onBottomClick(net.swofty.type.generic.gui.v2.context.ClickContext<AccessoryBagState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockItem cursorItem = new SkyBlockItem(player.getInventory().getCursorItem());
        SkyBlockItem clickedItem = new SkyBlockItem(player.getInventory().getItemStack(click.slot()));

        if (isItemAllowed(cursorItem) && isItemAllowed(clickedItem)) return true;

        player.sendMessage(Text.key("gui_sbmenu.bags.accessory.msg.cannot_put"));
        return false;
    }

    public static int getCollectionSlots(SkyBlockPlayer player) {
        return AccessoryBagSlots.BASE + AccessoryBagSlots.redstone(player);
    }

    public static int getJacobusSlots(SkyBlockPlayer player) {
        return AccessoryBagSlots.jacobus(player);
    }

    private static boolean isItemAllowed(SkyBlockItem item) {
        if (item.isNA() || item.getMaterial().equals(Material.AIR)) return true;
        return item.hasComponent(AccessoryComponent.class);
    }

    public record AccessoryBagState(int page) {
    }
}
