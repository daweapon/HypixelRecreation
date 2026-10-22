package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class TeleportPadDestinationView extends TeleportPadPickerView {
    public TeleportPadDestinationView(SkyBlockIsland island, TeleportPad pad, int page) {
        super(island, pad, "Set Destination", page);
    }

    private List<TeleportPad> destinations() {
        return TeleportPads.of(island).othersThan(pad);
    }

    @Override
    int entryCount() {
        return destinations().size();
    }

    @Override
    ItemStack.Builder entry(int index) {
        TeleportPad target = destinations().get(index);
        boolean selected = target.key().equals(pad.destination());
        return ItemStacks.item(target.iconType().material(), """
                %s
                <7>Click to set the Teleport Pad's
                <7>destination to %s<7>!

                %s""".formatted(target.displayName(), target.displayName(),
                selected ? "<a>SELECTED!" : "<e>Click to change!"));
    }

    @Override
    void choose(int index) {
        pad.destination(destinations().get(index).key());
    }

    @Override
    TeleportPadPickerView atPage(int page) {
        return new TeleportPadDestinationView(island, pad, page);
    }
}
