package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.bedwarsgame.util.BedWarsInventoryManipulator;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

// Base class for all Lucky Block reward effects, providing a name and shared item-giving helper.
public abstract class LuckyReward {
    private final String name;

    protected LuckyReward(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public abstract void apply(BedWarsPlayer player, Pos openedAt);

    protected void give(BedWarsPlayer player, ItemStack item) {
        BedWarsInventoryManipulator.addItemWithHotbarPriority(player, item, DatapointBedWarsHotbar.HotbarItemType.UTILITY);
    }

    protected static ItemStack named(Material material, String markup) {
        return ItemStacks.named(material, markup).build();
    }
}
