package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUISettingsCategory extends StatelessView {

    private static final int TOGGLE_OFFSET = 9;

    private final SettingsCategory category;

    public GUISettingsCategory(SettingsCategory category) {
        this.category = category;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> category.title(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        for (SettingEntry entry : category.entries()) {
            layout.slot(entry.slot(), (s, c) -> ItemStacks.item(entry.material(),
                    "<a>" + entry.name() + "\n<7>" + entry.description().replace("\n", "\n<7>")));

            layout.slot(entry.slot() + TOGGLE_OFFSET, (s, c) -> {
                boolean enabled = entry.reader().test((HypixelPlayer) c.player());
                return ItemStacks.item(enabled ? Material.LIME_DYE : Material.GRAY_DYE,
                        "<a>" + entry.name() + "\n<7>Click to " + (enabled ? "disable" : "enable") + "!");
            }, (click, c) -> {
                HypixelPlayer player = (HypixelPlayer) c.player();
                entry.writer().accept(player, !entry.reader().test(player));
                c.replace(new GUISettingsCategory(category));
            });
        }

        Components.backOrClose(layout, 49, ctx);
    }
}
