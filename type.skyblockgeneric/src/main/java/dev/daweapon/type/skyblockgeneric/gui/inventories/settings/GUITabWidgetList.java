package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.tab.TabLayout;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;

import java.util.List;

public class GUITabWidgetList extends StatelessView {
    private static final int[] COLUMN_SLOTS = {3, 4, 5};
    private static final int PRIORITY_SLOT = 13;

    private final TabLocation location;
    private final String selectedId;

    public GUITabWidgetList(TabLocation location, String selectedId) {
        this.location = location;
        this.selectedId = selectedId;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> location.widgetsTitle(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        for (int column = 0; column < TabLayout.WIDGET_COLUMNS; column++) {
            int index = column;
            layout.slot(COLUMN_SLOTS[column], (s, c) -> TabGui.toggle("Column " + (index + 2),
                    "<7>Shows or hides column " + (index + 2) + " of\n<7>the widget tab list.",
                    TabSettings.of(c.player()).columnEnabled(location, index)), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                settings.setColumnEnabled(location, index, !settings.columnEnabled(location, index));
                c.replace(new GUITabWidgetList(location, selectedId));
            });
        }

        layout.slot(PRIORITY_SLOT, (s, c) -> priorityChanger(TabSettings.of(c.player())), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            List<TabWidget> movable = settings.available(location).stream().filter(w -> !w.alwaysOn()).toList();
            if (movable.isEmpty()) return;
            int current = indexOf(movable, selectedId);
            if (click.click() instanceof Click.LeftShift) {
                if (current >= 0) settings.move(location, movable.get(current), 1);
                c.replace(new GUITabWidgetList(location, selectedId));
            } else if (click.click() instanceof Click.RightShift) {
                if (current >= 0) settings.move(location, movable.get(current), -1);
                c.replace(new GUITabWidgetList(location, selectedId));
            } else {
                int step = click.click() instanceof Click.Right ? -1 : 1;
                int next = current < 0 ? (step > 0 ? 0 : movable.size() - 1)
                        : Math.floorMod(current + step, movable.size());
                c.replace(new GUITabWidgetList(location, movable.get(next).id()));
            }
        });

        List<TabWidget> widgets = TabSettings.of(ctx.player()).available(location);
        for (int i = 0; i < widgets.size() && i < TabGui.WIDGET_GRID.length; i++) {
            TabWidget widget = widgets.get(i);
            layout.slot(TabGui.WIDGET_GRID[i], (s, c) -> widgetItem(TabSettings.of(c.player()), widget), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                if (click.click() instanceof Click.Right || click.click() instanceof Click.RightShift) {
                    c.push(new GUITabWidgetSettings(location, widget));
                    return;
                }
                if (widget.alwaysOn()) return;
                settings.setEnabled(location, widget, !settings.enabled(location, widget));
                c.replace(new GUITabWidgetList(location, selectedId));
            });
        }

        Components.back(layout, TabGui.BACK_SLOT, ctx);
        Components.close(layout, TabGui.CLOSE_SLOT);
        layout.slot(TabGui.PREVIEW_SLOT, (s, c) -> TabGui.preview((SkyBlockPlayer) c.player(), location));
        layout.slot(TabGui.RESET_SLOT, (s, c) -> ItemStacks.item(Material.TNT,
                "<c>Reset All " + location.getDisplayName() + " Settings\n<7>Reset all " + location.getDisplayName()
                        + " tablist widget\n<7>settings back to their default.\n\n<e>Click to reset ALL " + location.getDisplayName() + " settings!"),
                (click, c) -> {
                    TabSettings.of(c.player()).resetLocation(location);
                    c.replace(new GUITabWidgetList(location, null));
                });
    }

    private net.minestom.server.item.ItemStack.Builder widgetItem(TabSettings settings, TabWidget widget) {
        boolean enabled = settings.enabled(location, widget);
        String current = widget.alwaysOn() ? "<a>ALWAYS ENABLED" : TabGui.status(enabled);
        String prefix = enabled ? "<a>✔ " : "<c>✖ ";
        String action = widget.alwaysOn() ? "<e>Right-click to change settings!"
                : "<e>Left-click to " + (enabled ? "disable" : "enable") + " this widget!\n<e>Right-click to change settings!";
        return ItemStacks.item(widget.icon(), prefix + widget.displayName() + " Widget\n<7>Currently: " + current
                + "\n\n<7>" + widget.description().replace("\n", "\n<7>") + "\n\n" + action);
    }

    private net.minestom.server.item.ItemStack.Builder priorityChanger(TabSettings settings) {
        StringBuilder body = new StringBuilder("<a>Priority Changer\n<7>Change the order of the widgets\n<7>that are shown ")
                .append(location.widgetsTitle().startsWith("Widgets on") ? "on " : "in ")
                .append(location.getDisplayName()).append(".\n");
        for (TabWidget widget : settings.available(location)) {
            body.append("\n<8>▪ <7>").append(widget.displayName()).append(" Widget");
            if (widget.alwaysOn()) body.append(" <c>(LOCKED)");
            else if (widget.id().equals(selectedId)) body.append(" <a>(EDITING)");
        }
        body.append("\n\n<e>Left/Right-click to select a widget!\n<e>Shift Left/Right-click to change its order!");
        return ItemStacks.item(Material.HOPPER, body.toString());
    }

    private static int indexOf(List<TabWidget> widgets, String id) {
        for (int i = 0; i < widgets.size(); i++) if (widgets.get(i).id().equals(id)) return i;
        return -1;
    }
}
