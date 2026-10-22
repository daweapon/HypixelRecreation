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
import net.minestom.server.item.Material;

public class TeleportPadDirectionView extends StatelessView {
    private static final int FIRST_SLOT = 9;
    private static final int BACK_SLOT = 22;

    private final SkyBlockIsland island;
    private final TeleportPad pad;

    public TeleportPadDirectionView(SkyBlockIsland island, TeleportPad pad) {
        this.island = island;
        this.pad = pad;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Set Direction", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        TeleportPadDirection[] directions = TeleportPadDirection.values();
        for (int i = 0; i < directions.length; i++) {
            TeleportPadDirection direction = directions[i];
            layout.slot(FIRST_SLOT + i, (_, _) -> ItemStacks.item(
                            direction == TeleportPadDirection.PLAYER ? Material.PLAYER_HEAD : Material.COMPASS, """
                            <a>%s
                            <7>Click to set the direction you
                            <7>face when you arrive!

                            %s""".formatted(direction.label(),
                            direction == pad.direction() ? "<a>SELECTED!" : "<e>Click to change!")),
                    (_, c) -> {
                        pad.direction(direction);
                        TeleportPads.save(island);
                        ViewNavigator.get(c.player()).pop();
                    });
        }
        Components.backOrClose(layout, BACK_SLOT, ctx);
    }
}
