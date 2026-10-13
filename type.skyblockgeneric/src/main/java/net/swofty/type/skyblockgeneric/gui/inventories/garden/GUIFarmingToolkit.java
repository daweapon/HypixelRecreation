package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.tool.FarmingToolkit;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIFarmingToolkit extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Farming Toolkit"), InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        for (FarmingToolkit.Slot slot : FarmingToolkit.SLOTS) {
            layout.slot(slot.guiSlot(), (_, c) -> render((SkyBlockPlayer) c.player(), slot), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (FarmingToolkit.retrieve(player, slot)) c.session(Object.class).refresh();
            });
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        if (FarmingToolkit.store(player, click.slot())) ctx.session(Object.class).refresh();
        return false;
    }

    private ItemStack.Builder render(SkyBlockPlayer player, FarmingToolkit.Slot slot) {
        SkyBlockItem stored = FarmingToolkit.stored(Gardens.profile(player), slot);
        if (stored == null) {
            return GardenMenus.item(Material.GRAY_DYE, Text.of("<c>" + slot.name()),
                    List.of(Text.of("<7>Click on a valid tool in your"), Text.of("<7>inventory to add it to your toolkit!")));
        }
        return PlayerItemUpdater.playerUpdate(player, stored.getItemStack(), true);
    }
}
