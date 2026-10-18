package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import net.minestom.server.inventory.InventoryType;

import java.util.List;

public class GUITabWidgetShown extends StatelessView {
    private final TabLocation location;
    private final TabWidget widget;

    public GUITabWidgetShown(TabLocation location, TabWidget widget) {
        this.location = location;
        this.widget = widget;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Shown " + widget.displayName() + " Setting",
                InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        List<TabWidgetToggle> toggles = widget.toggles();
        for (int i = 0; i < toggles.size() && i < TabGui.GRID.length; i++) {
            TabWidgetToggle toggle = toggles.get(i);
            layout.slot(TabGui.GRID[i], (s, c) -> TabGui.toggle(toggle.displayName(),
                    "<7>Choose whether this is shown. Widgets\n<7>with Minimize on show only these.",
                    shown(c.player(), toggle)), (click, c) -> {
                TabSettings.of(c.player()).setToggle(location, widget, toggle.id(), !shown(c.player(), toggle));
                c.replace(new GUITabWidgetShown(location, widget));
            });
        }

        Components.backOrClose(layout, 40, ctx);
    }

    private boolean shown(dev.daweapon.type.generic.user.HypixelPlayer player, TabWidgetToggle toggle) {
        return TabSettings.of(player).toggleValue(location, widget, toggle.id());
    }
}
