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

import java.util.ArrayList;
import java.util.List;

public class GUIHuntingToolkit extends StatelessView {
    private static final int[] SLOTS = {11, 12, 14, 15, 20};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Hunting Toolkit"), InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        layout.slot(41, ItemStacks.item(Material.REDSTONE_TORCH, """
                <a>Toolkit Information
                <7>Click on a valid Hunting Tool in your
                <7>inventory while this menu is open to
                <7>store it in your toolkit.

                <e>Sneak + Right-click a checked out
                <e>tool to swap between your stored
                <e>tools."""));
        for (int i = 0; i < HuntingToolkit.SLOTS.size(); i++) {
            HuntingToolkit.Slot slot = HuntingToolkit.SLOTS.get(i);
            layout.slot(SLOTS[i], (_, c) -> render((SkyBlockPlayer) c.player(), slot), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                boolean right = click.click() instanceof Click.Right || click.click() instanceof Click.RightShift;
                if (right && slot.isTrap() && HuntingToolkit.stored(player, slot) != null) {
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

    private static String source(HuntingToolkit.Slot slot) {
        return switch (slot.group()) {
            case HUNTAXE -> "<b>Alan <7>and <a>Vinesap Collection";
            case BLACK_HOLE -> "<b>Albert";
            case LASSO -> "<a>Vinesap Collection";
            case FISHING_NET -> "<b>Jaeger <7>and <f>Sea Lumies Collection";
            case HUNTRAP -> "";
        };
    }

    private ItemStack.Builder render(SkyBlockPlayer player, HuntingToolkit.Slot slot) {
        SkyBlockItem stored = HuntingToolkit.stored(player, slot);
        if (stored == null) {
            List<Text> lore = new ArrayList<>();
            if (slot.isTrap()) {
                lore.add(Text.of("<8>Huntrap #1"));
            } else {
                lore.add(Text.of("<7>Obtained from: {}", source(slot)));
            }
            lore.add(Text.empty());
            lore.add(Text.of("<7>Click on a valid tool in your"));
            lore.add(Text.of("<7>inventory to add it to your toolkit!"));
            return ItemStacks.item(Material.GRAY_DYE, Text.of(slot.isTrap() ? "<c>No Trap stored!" : "<a>" + slot.name()), lore);
        }
        return PlayerItemUpdater.playerUpdate(player, stored.getItemStack(), true);
    }
}
