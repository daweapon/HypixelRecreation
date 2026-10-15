package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.PaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Picks which owned pet goes into an Exp Share slot
public class GUIPetExpShareSelect extends PaginatedView<SkyBlockItem, GUIPetExpShareSelect.SelectState> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final int shareSlot;

    public GUIPetExpShareSelect(int shareSlot) {
        this.shareSlot = shareSlot;
    }

    @Override
    public ViewConfiguration<SelectState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.pets.exp_sharing.select.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockItem item, int index, HypixelPlayer player) {
        ItemStack.Builder itemStack = new NonPlayerItemUpdater(item).getUpdatedItem();
        List<Text> lore = ItemStacks.loreText(itemStack);
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_sbmenu.pets.exp_sharing.select.click"));
        return ItemStacks.lore(itemStack, lore);
    }

    @Override
    protected void onItemClick(ClickContext<SelectState> click, ViewContext ctx, SkyBlockItem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        player.getPetData().setExpSharePet(shareSlot, item);
        player.sendMessage(Text.key("gui_sbmenu.pets.exp_sharing.msg.added", item.getDisplayName(), shareSlot + 1));
        ctx.pop();
    }

    @Override
    protected boolean shouldFilterFromSearch(SelectState state, SkyBlockItem item) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<SelectState> layout, SelectState state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    public static SelectState createInitialState(SkyBlockPlayer player) {
        return new SelectState(List.copyOf(player.getPetData().getPetsMap().keySet()), 0);
    }

    public record SelectState(List<SkyBlockItem> items, int page) implements PaginatedState<SkyBlockItem> {
        @Override
        public PaginatedState<SkyBlockItem> withPage(int page) {
            return new SelectState(items, page);
        }

        @Override
        public PaginatedState<SkyBlockItem> withItems(List<SkyBlockItem> items) {
            return new SelectState(items, page);
        }
    }
}
