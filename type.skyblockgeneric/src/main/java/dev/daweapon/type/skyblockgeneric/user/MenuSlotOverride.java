package dev.daweapon.type.skyblockgeneric.user;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Activities that put their own item in the SkyBlock Menu's hotbar slot, as a dungeon run's Magical Map does. By
 * default the stand-in still opens the SkyBlock Menu, as Hypixel's map does on right-click; one registered with an
 * action runs that instead, as a Carnival game's Leave Game button does.
 */
public final class MenuSlotOverride {
    private record Override(Function<SkyBlockPlayer, @Nullable ItemStack> provider, @Nullable Consumer<SkyBlockPlayer> action) {
    }

    private static final List<Override> OVERRIDES = new CopyOnWriteArrayList<>();

    private MenuSlotOverride() {
    }

    public static void register(Function<SkyBlockPlayer, @Nullable ItemStack> provider) {
        OVERRIDES.add(new Override(provider, null));
    }

    public static void register(Function<SkyBlockPlayer, @Nullable ItemStack> provider, Consumer<SkyBlockPlayer> action) {
        OVERRIDES.add(new Override(provider, action));
    }

    /** Whether this stack is the player's current stand-in, which opens the SkyBlock Menu like the item it replaced. */
    public static boolean opensMenu(SkyBlockPlayer player, @Nullable ItemStack stack) {
        Override current = current(player);
        return current != null && current.action() == null && matches(current, player, stack);
    }

    /** Runs the current stand-in's own action when this stack is that stand-in; returns whether it did. */
    public static boolean use(SkyBlockPlayer player, @Nullable ItemStack stack) {
        Override current = current(player);
        if (current == null || current.action() == null || !matches(current, player, stack)) return false;
        current.action().accept(player);
        return true;
    }

    public static @Nullable ItemStack itemFor(SkyBlockPlayer player) {
        Override current = current(player);
        return current == null ? null : current.provider().apply(player);
    }

    private static @Nullable Override current(SkyBlockPlayer player) {
        for (Override override : OVERRIDES) {
            if (override.provider().apply(player) != null) return override;
        }
        return null;
    }

    private static boolean matches(Override override, SkyBlockPlayer player, @Nullable ItemStack stack) {
        if (stack == null || stack.isAir()) return false;
        ItemStack standIn = override.provider().apply(player);
        return standIn != null && standIn.equals(stack);
    }
}
