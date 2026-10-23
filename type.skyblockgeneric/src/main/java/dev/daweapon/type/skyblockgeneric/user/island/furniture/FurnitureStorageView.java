package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class FurnitureStorageView extends StatelessView {
    private final IslandFurniture owner;
    private final PlacedFurniture furniture;
    private final int pages;
    private final int page;
    private boolean switching;

    public FurnitureStorageView(IslandFurniture owner, PlacedFurniture furniture, int pages, int page) {
        this.owner = owner;
        this.furniture = furniture;
        this.pages = Math.max(1, pages);
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(StringUtility.toNormalCase(furniture.itemId()) + " (Page " + (page + 1) + "/" + pages + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        List<ItemStack> stacks = FurnitureContents.page(owner.storageOf(furniture), page);
        for (int slot = 0; slot < FurnitureContents.PAGE_SLOTS; slot++) {
            layout.editable(slot, stacks.get(slot).isAir() ? ItemStack.builder(Material.AIR) : stacks.get(slot).builder(), (_, _, _, _) -> {
            });
        }
        for (int slot = FurnitureContents.PAGE_SLOTS; slot < 54; slot++) {
            layout.slot(slot, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        }
        Components.close(layout, 49);
        if (page > 0) {
            layout.slot(45, ItemStacks.item(Material.ARROW, 1, "<a>Previous Page\n<7>Page " + page), (_, c) -> turn(c, page - 1));
        }
        if (page < pages - 1) {
            layout.slot(53, ItemStacks.item(Material.ARROW, 1, "<a>Next Page\n<7>Page " + (page + 2)), (_, c) -> turn(c, page + 1));
        }
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (switching) return;
        save(ctx);
    }

    private void turn(ViewContext ctx, int target) {
        save(ctx);
        switching = true;
        ctx.player().openView(new FurnitureStorageView(owner, furniture, pages, target));
    }

    private void save(ViewContext ctx) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < FurnitureContents.PAGE_SLOTS; slot++) stacks.add(ctx.inventory().getItemStack(slot));
        owner.setStorage(furniture, FurnitureContents.withPage(owner.storageOf(furniture), pages, page, stacks));
    }

    @Override
    public boolean onBottomClick(dev.daweapon.type.generic.gui.v2.context.ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }
}
