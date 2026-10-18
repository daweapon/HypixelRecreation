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
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUITabWidgetSettings extends StatelessView {
    private static final int APPLY_ALL_SLOT = 53;

    private final TabLocation location;
    private final TabWidget widget;

    public GUITabWidgetSettings(TabLocation location, TabWidget widget) {
        this.location = location;
        this.widget = widget;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> widget.displayName() + " Widget Settings",
                InventoryType.CHEST_6_ROW);
    }

    private String scope() {
        return "<7>Changing this widget's setting will\n<7>only apply to being in " + location.getDisplayName() + ".";
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(12, (s, c) -> TabGui.toggle("Spacing",
                "<7>Adds an empty line after this widget.\n\n" + scope(),
                TabSettings.of(c.player()).spacing(location, widget)), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setSpacing(location, widget, !settings.spacing(location, widget));
            c.replace(new GUITabWidgetSettings(location, widget));
        });

        layout.slot(13, (s, c) -> TabGui.toggle("Wrapping",
                "<7>Lets this widget continue in the next\n<7>column.\n\n" + scope(),
                TabSettings.of(c.player()).wrapping(location, widget)), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setWrapping(location, widget, !settings.wrapping(location, widget));
            c.replace(new GUITabWidgetSettings(location, widget));
        });

        if (!widget.toggles().isEmpty()) {
            layout.slot(14, (s, c) -> TabGui.toggle("Minimize",
                    "<7>Only show the information you chose\n<7>below.\n\n" + scope(),
                    TabSettings.of(c.player()).minimized(location, widget)), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                settings.setMinimized(location, widget, !settings.minimized(location, widget));
                c.replace(new GUITabWidgetSettings(location, widget));
            });
        }

        List<TabWidgetToggle> toggles = widget.toggles();
        for (int i = 0; i < toggles.size() && i < TabGui.WIDGET_GRID.length; i++) {
            TabWidgetToggle toggle = toggles.get(i);
            layout.slot(TabGui.WIDGET_GRID[i], (s, c) -> TabGui.toggle("Show " + toggle.displayName(),
                    "<7>Shows your " + toggle.displayName() + " on this widget.\n\n" + scope(),
                    TabSettings.of(c.player()).toggleValue(location, widget, toggle.id())), (click, c) -> {
                TabSettings settings = TabSettings.of(c.player());
                settings.setToggle(location, widget, toggle.id(), !settings.toggleValue(location, widget, toggle.id()));
                c.replace(new GUITabWidgetSettings(location, widget));
            });
        }

        Components.back(layout, TabGui.BACK_SLOT, ctx);
        Components.close(layout, TabGui.CLOSE_SLOT);
        for (int previewSlot : TabGui.PREVIEW_SLOTS) {
            layout.slot(previewSlot, (s, c) -> TabGui.preview((SkyBlockPlayer) c.player(), location));
        }
        layout.slot(APPLY_ALL_SLOT, (s, c) -> ItemStacks.item(Material.PLAYER_HEAD,
                "<a>Apply Settings To All Modes\n<7>You are currently only editing the\n<7>" + widget.displayName()
                        + " Widget settings for " + location.getDisplayName() + ".\n<7>Click this button to apply these\n<7>settings to all other modes.\n\n<e>Click to apply settings!"),
                (click, c) -> {
                    TabSettings.of(c.player()).applyToAllLocations(location, widget);
                    c.player().sendMessage("<a>Applied the {} Widget settings to all modes!", widget.displayName());
                });
    }
}
