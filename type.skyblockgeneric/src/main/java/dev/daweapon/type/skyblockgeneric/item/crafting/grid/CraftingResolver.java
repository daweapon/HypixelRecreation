package dev.daweapon.type.skyblockgeneric.item.crafting.grid;

import dev.daweapon.type.generic.gui.v2.click.SlotStack;

public interface CraftingResolver {

    SlotStack result(SlotStack[] grid);

    SlotStack[] consume(SlotStack[] grid);
}
