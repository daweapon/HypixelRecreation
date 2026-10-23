package dev.daweapon.type.skyblockgeneric.gui.inventories.leaderboard;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.leaderboard.BoardCategory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

import java.util.List;

public class GUILeaderboards extends StatelessView {
    private static final int[] SLOTS = {10, 12, 14, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Leaderboards", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        BoardCategory[] categories = BoardCategory.values();
        for (int i = 0; i < categories.length; i++) {
            BoardCategory category = categories[i];
            layout.slot(SLOTS[i], (_, _) -> ItemStacks.item(category.getIcon(), Text.of(category.getColour() + "{}", category.getDisplayName()), List.of(
                            Text.of("<7>Browse the {} leaderboards.", category.getDisplayName().toLowerCase()),
                            Text.empty(),
                            Text.of("<e>Click to view!"))),
                    (_, c) -> GUILeaderboardBoards.open((SkyBlockPlayer) c.player(), category));
        }
        layout.slot(22, (_, _) -> ItemStacks.item(net.minestom.server.item.Material.BOOK, Text.of("<a>About"), List.of(
                Text.of("<7>Rankings are per profile and update"),
                Text.of("<7>whenever your profile saves."),
                Text.empty(),
                Text.of("<8>Staff are not ranked."))));
    }
}
