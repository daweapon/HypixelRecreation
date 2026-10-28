package dev.daweapon.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.hunting.HuntingToolkit;
import dev.daweapon.type.skyblockgeneric.hunting.HuntrapService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIHuntingToolkit extends StatelessView {
    private static final int[] SLOTS = {11, 12, 13, 14, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Hunting Toolkit"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        for (int i = 0; i < HuntingToolkit.SLOTS.size(); i++) {
            HuntingToolkit.Slot slot = HuntingToolkit.SLOTS.get(i);
            layout.slot(SLOTS[i], (_, c) -> render((SkyBlockPlayer) c.player(), slot), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                boolean right = click.click() instanceof Click.Right || click.click() instanceof Click.RightShift;
                if (right && slot.trap() != null && HuntingToolkit.stored(player, slot) != null) {
                    player.closeInventory();
                    HuntrapService.deployFromToolkit(player, slot);
                } else if (HuntingToolkit.retrieve(player, slot)) c.session(Object.class).refresh();
            });
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        if (HuntingToolkit.store(player, click.slot())) ctx.session(Object.class).refresh();
        return false;
    }

    private ItemStack.Builder render(SkyBlockPlayer player, HuntingToolkit.Slot slot) {
        SkyBlockItem stored = HuntingToolkit.stored(player, slot);
        if (stored == null) {
            return ItemStacks.item(Material.GRAY_DYE, Text.of("<c>" + slot.name()), List.of(
                    Text.of("<7>Click on a valid tool in your"), Text.of("<7>inventory to add it to your toolkit!")));
        }
        return PlayerItemUpdater.playerUpdate(player, stored.getItemStack(), true);
    }
}
