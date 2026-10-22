package dev.daweapon.type.skyblockgeneric.island.pad;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.item.ItemStack;

public class TeleportPadIconView extends TeleportPadPickerView {
    public TeleportPadIconView(SkyBlockIsland island, TeleportPad pad, int page) {
        super(island, pad, "Set Icon", page);
    }

    @Override
    int entryCount() {
        return TeleportPadIcon.all().size();
    }

    @Override
    ItemStack.Builder entry(int index) {
        TeleportPadIcon icon = TeleportPadIcon.get(index);
        return ItemStacks.item(icon.material(), """
                %s
                <7>Click to set the Teleport Pad's
                <7>icon!

                %s""".formatted(icon.label(), index == pad.icon() ? "<a>SELECTED!" : "<e>Click to change!"));
    }

    @Override
    void choose(int index) {
        pad.icon(index);
    }

    @Override
    TeleportPadPickerView atPage(int page) {
        return new TeleportPadIconView(island, pad, page);
    }
}
