package dev.daweapon.type.skyblockgeneric.gui.inventories.spraycan;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spray.Spray;
import dev.daweapon.type.skyblockgeneric.spray.SprayCanData;
import dev.daweapon.type.skyblockgeneric.spray.SprayDirection;
import dev.daweapon.type.skyblockgeneric.spray.SprayInk;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;
import java.util.function.Consumer;

/** Reads and rewrites the Spray Can sitting in one of the player's inventory slots. */
final class SprayCans {
    private SprayCans() {}

    static Optional<SprayCanData> read(SkyBlockPlayer player, int slot) {
        return can(player, slot).map(SprayCanData::of);
    }

    static void select(SkyBlockPlayer player, int slot, Spray spray, SprayDirection direction) {
        edit(player, slot, data -> data.select(spray, direction));
        player.sendMessage("<a>Selected <e>" + spray.name() + (spray.hasDirections() ? " <7>(" + direction.getDisplayName() + ")" : "") + "<a>!");
    }

    static int refill(SkyBlockPlayer player, int slot, SprayInk dye, int dyes) {
        int[] used = {0};
        edit(player, slot, data -> used[0] = data.refill(dye, dyes));
        return used[0];
    }

    private static void edit(SkyBlockPlayer player, int slot, Consumer<SprayCanData> change) {
        can(player, slot).ifPresent(item -> {
            SprayCanData data = SprayCanData.of(item);
            change.accept(data);
            data.writeTo(item);
            player.getInventory().setItemStack(slot, item.getItemStack());
        });
    }

    private static Optional<SkyBlockItem> can(SkyBlockPlayer player, int slot) {
        if (slot < 0 || slot >= player.getInventory().getSize()) return Optional.empty();
        ItemStack stack = player.getInventory().getItemStack(slot);
        if (stack.isAir()) return Optional.empty();
        SkyBlockItem item = new SkyBlockItem(stack);
        return item.getAttributeHandler().getPotentialType() == ItemType.SPRAY_CAN ? Optional.of(item) : Optional.empty();
    }
}
