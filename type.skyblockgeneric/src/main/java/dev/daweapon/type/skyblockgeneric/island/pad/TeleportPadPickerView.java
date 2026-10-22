package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

abstract class TeleportPadPickerView extends StatelessView {
    static final int PAGE_SIZE = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int BACK_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    protected final SkyBlockIsland island;
    protected final TeleportPad pad;
    private final String title;
    private final int page;

    TeleportPadPickerView(SkyBlockIsland island, TeleportPad pad, String title, int page) {
        this.island = island;
        this.pad = pad;
        this.title = title;
        this.page = page;
    }

    abstract int entryCount();

    abstract ItemStack.Builder entry(int index);

    abstract void choose(int index);

    abstract TeleportPadPickerView atPage(int page);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        int first = page * PAGE_SIZE;
        for (int slot = 0; slot < PAGE_SIZE && first + slot < entryCount(); slot++) {
            int index = first + slot;
            layout.slot(slot, (_, _) -> entry(index), (_, c) -> {
                choose(index);
                TeleportPads.save(island);
                ViewNavigator.get(c.player()).pop();
            });
        }
        Components.backOrClose(layout, BACK_SLOT, ctx);
        if (page > 0) {
            layout.slot(PREVIOUS_SLOT, (_, _) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                    (_, c) -> ViewNavigator.get(c.player()).replace(atPage(page - 1)));
        }
        if (first + PAGE_SIZE < entryCount()) {
            layout.slot(NEXT_SLOT, (_, _) -> ItemStacks.item(Material.ARROW, "<a>Next Page"),
                    (_, c) -> ViewNavigator.get(c.player()).replace(atPage(page + 1)));
        }
    }
}
