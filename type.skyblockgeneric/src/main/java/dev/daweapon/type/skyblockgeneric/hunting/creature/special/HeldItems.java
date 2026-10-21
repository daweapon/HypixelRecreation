package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.garden.pest.PestVacuum;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class HeldItems {
    private HeldItems() {
    }

    static @Nullable SkyBlockItem held(SkyBlockPlayer player) {
        ItemStack stack = player.getItemInMainHand();
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return null;
        return new SkyBlockItem(stack);
    }

    static boolean holds(SkyBlockPlayer player, String id) {
        SkyBlockItem item = held(player);
        return item != null && id.equals(item.getAttributeHandler().getTypeAsString());
    }

    static boolean holdsVacuum(SkyBlockPlayer player) {
        SkyBlockItem item = held(player);
        return item != null && PestVacuum.tier(item) != null;
    }

    static boolean wearsHelmet(SkyBlockPlayer player, String id) {
        ItemStack helmet = player.getHelmet();
        if (helmet.isAir() || !SkyBlockItem.isSkyBlockItem(helmet)) return false;
        return id.equals(new SkyBlockItem(helmet).getAttributeHandler().getTypeAsString());
    }
}
