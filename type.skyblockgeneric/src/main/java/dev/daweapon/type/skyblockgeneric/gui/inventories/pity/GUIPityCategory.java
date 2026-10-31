package dev.daweapon.type.skyblockgeneric.gui.inventories.pity;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.pity.PityCategory;
import dev.daweapon.type.skyblockgeneric.pity.PityItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

public class GUIPityCategory extends StatelessView {
    public static final int BACK_SLOT = 48;
    public static final int CLOSE_SLOT = 49;

    private final PityCategory category;

    public GUIPityCategory(PityCategory category) {
        this.category = category;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(category.menuTitle(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, BACK_SLOT, ctx);
        Components.close(layout, CLOSE_SLOT);
        for (PityItem item : category.items()) {
            layout.slot(item.slot(), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                return ItemStacks.item(item.icon(), Text.of(item.title()),
                        GUIPity.texts(item.loreFor(category.id(), player)));
            });
        }
    }
}
