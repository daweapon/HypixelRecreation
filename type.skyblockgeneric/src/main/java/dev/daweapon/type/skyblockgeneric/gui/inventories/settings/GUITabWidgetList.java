package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUITabWidgetList extends StatelessView {
    private final TabLocation location;
    private final int page;

    public GUITabWidgetList(TabLocation location, int page) {
        this.location = location;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Widgets on " + location.getDisplayName(),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        List<TabWidget> widgets = TabSettings.of(ctx.player()).available(location);
        int pageSize = TabGui.GRID.length;
        int pages = Math.max(1, (widgets.size() + pageSize - 1) / pageSize);
        int start = Math.min(page, pages - 1) * pageSize;

        for (int i = 0; i < pageSize && start + i < widgets.size(); i++) {
            TabWidget widget = widgets.get(start + i);
            layout.slot(TabGui.GRID[i], (s, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                String current = widget.alwaysOn() ? "<a>ALWAYS ENABLED"
                        : TabGui.status(settings.enabled(location, widget));
                return ItemStacks.item(widget.icon(), "<a>" + widget.displayName()
                        + "\n<7>Currently: " + current + "\n\n<e>Click to edit!");
            }, (click, c) -> c.push(new GUITabWidgetSettings(location, widget)));
        }

        if (page > 0) {
            layout.slot(48, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                    (click, c) -> c.replace(new GUITabWidgetList(location, page - 1)));
        }
        if (page < pages - 1) {
            layout.slot(50, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Next Page"),
                    (click, c) -> c.replace(new GUITabWidgetList(location, page + 1)));
        }

        Components.backOrClose(layout, 49, ctx);
    }
}
