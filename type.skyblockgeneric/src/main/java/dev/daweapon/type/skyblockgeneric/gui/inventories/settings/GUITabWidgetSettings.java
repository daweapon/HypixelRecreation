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

public class GUITabWidgetSettings extends StatelessView {
    private final TabLocation location;
    private final TabWidget widget;

    public GUITabWidgetSettings(TabLocation location, TabWidget widget) {
        this.location = location;
        this.widget = widget;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> widget.displayName() + " Widget Settings",
                InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        if (widget.alwaysOn()) {
            layout.slot(10, (s, c) -> ItemStacks.item(Material.LIME_DYE,
                    "<a>Enabled\n<7>This widget is always enabled\n<7>and cannot be moved."), (click, c) -> {
            });
        } else {
            layout.slot(10, (s, c) -> TabGui.toggle("Enabled",
                    "<7>Show this widget on the tab list.",
                    TabSettings.of(c.player()).enabled(location, widget)), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                settings.setEnabled(location, widget, !settings.enabled(location, widget));
                c.replace(new GUITabWidgetSettings(location, widget));
            });
        }

        layout.slot(11, (s, c) -> TabGui.toggle("Spacing",
                "<7>Adds an empty line after this widget.",
                TabSettings.of(c.player()).spacing(location, widget)), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setSpacing(location, widget, !settings.spacing(location, widget));
            c.replace(new GUITabWidgetSettings(location, widget));
        });

        layout.slot(12, (s, c) -> TabGui.toggle("Wrapping",
                "<7>Lets this widget continue in the\n<7>next column.",
                TabSettings.of(c.player()).wrapping(location, widget)), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setWrapping(location, widget, !settings.wrapping(location, widget));
            c.replace(new GUITabWidgetSettings(location, widget));
        });

        if (!widget.toggles().isEmpty()) {
            layout.slot(13, (s, c) -> TabGui.toggle("Minimize",
                    "<7>Only show the information you\n<7>chose in the shown settings.",
                    TabSettings.of(c.player()).minimized(location, widget)), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                settings.setMinimized(location, widget, !settings.minimized(location, widget));
                c.replace(new GUITabWidgetSettings(location, widget));
            });

            layout.slot(14, (s, c) -> ItemStacks.item(Material.WRITABLE_BOOK,
                    "<a>Shown Information\n<7>Choose what a minimized widget\n<7>displays.\n\n<e>Click to edit!"),
                    (click, c) -> c.push(new GUITabWidgetShown(location, widget)));
        }

        if (!widget.alwaysOn()) {
            layout.slot(15, (s, c) -> ItemStacks.item(Material.ARROW,
                    "<a>Move Earlier\n<7>Show this widget before the one\n<7>above it.\n\n<e>Click to move!"), (click, c) -> {
                TabSettings.of(c.player()).move(location, widget, -1);
                c.replace(new GUITabWidgetSettings(location, widget));
            });
            layout.slot(16, (s, c) -> ItemStacks.item(Material.ARROW,
                    "<a>Move Later\n<7>Show this widget after the one\n<7>below it.\n\n<e>Click to move!"), (click, c) -> {
                TabSettings.of(c.player()).move(location, widget, 1);
                c.replace(new GUITabWidgetSettings(location, widget));
            });
        }

        Components.backOrClose(layout, 31, ctx);
    }
}
