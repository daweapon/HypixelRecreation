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
import dev.daweapon.type.skyblockgeneric.pity.PityCatalogue;
import net.minestom.server.inventory.InventoryType;

import java.util.List;

public class GUIPity extends StatelessView {
    public static final int CLOSE_SLOT = 49;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Pity", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, CLOSE_SLOT);
        for (PityCategory category : PityCatalogue.categories()) {
            List<Text> lore = texts(category.lore());
            layout.slot(category.slot(), (_, _) -> ItemStacks.item(category.icon(), Text.of(category.title()), lore),
                    (_, c) -> c.push(new GUIPityCategory(category)));
        }
    }

    static List<Text> texts(List<String> lines) {
        return lines.stream().map(line -> line.isEmpty() ? Text.empty() : Text.of(line)).toList();
    }
}
