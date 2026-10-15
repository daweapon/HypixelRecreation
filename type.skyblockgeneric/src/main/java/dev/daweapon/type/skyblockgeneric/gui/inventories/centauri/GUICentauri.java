package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

// Developer/cheat menu linking to the creative item list, coin generator, and toy box
public final class GUICentauri implements View<GUICentauri.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Centauri", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (s, c) -> item("<a>Item List", Material.STICK,
                "<7>Browse and claim items by category:", "<7>Regular, Pets, Talismans, Materials.", " ", "<e>Click to view!"),
                (click, c) -> c.push(new GUICentauriItemCategories(), new GUICentauriItemCategories.State()));
        layout.slot(13, (s, c) -> item("<a>Coin Generator", Material.GOLD_BLOCK,
                "<7>Generate Coins at the click of a", "<7>button!", " ", "<e>Click to view!"),
                (click, c) -> c.push(new GUICentauriCoinGenerator(), new GUICentauriCoinGenerator.State()));
        layout.slot(15, (s, c) -> item("<a>Toy Box", Material.CHEST,
                "<7>A variety of useful and cheaty", "<7>utilities to make your life easier!", " ", "<e>Click to view!"),
                (click, c) -> c.push(new GUICentauriToyBox(), new GUICentauriToyBox.State()));
        Components.close(layout, 31);
    }

    static net.minestom.server.item.ItemStack.Builder item(String name, Material material, String... lore) {
        StringBuilder block = new StringBuilder(name);
        for (String line : lore) {
            block.append('\n').append(line);
        }
        return ItemStacks.item(material, 1, block.toString());
    }

    public record State() {}
}
