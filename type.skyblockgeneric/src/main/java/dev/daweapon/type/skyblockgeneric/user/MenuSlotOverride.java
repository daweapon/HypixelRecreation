package dev.daweapon.type.skyblockgeneric.user;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Activities that put their own item in the SkyBlock Menu's hotbar slot, as a dungeon run's Magical Map does. The stand-in
 * still opens the SkyBlock Menu, as Hypixel's map does on right-click.
 */
public final class MenuSlotOverride {
    private static final List<Function<SkyBlockPlayer, @Nullable ItemStack>> PROVIDERS = new CopyOnWriteArrayList<>();

    private MenuSlotOverride() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable ItemStack> provider) {
        PROVIDERS.add(provider);
    }

    /** Whether this stack is the player's current stand-in, which opens the SkyBlock Menu like the item it replaced. */
    public static boolean opensMenu(SkyBlockPlayer player, @Nullable ItemStack stack) {
        if (stack == null || stack.isAir()) return false;
        ItemStack standIn = itemFor(player);
        return standIn != null && standIn.equals(stack);
    }

    public static @Nullable ItemStack itemFor(SkyBlockPlayer player) {
        for (Function<SkyBlockPlayer, @Nullable ItemStack> provider : PROVIDERS) {
            ItemStack item = provider.apply(player);
            if (item != null) return item;
        }
        return null;
    }
}
