package dev.daweapon.type.skyblockgeneric.crimson.dojo;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * While a player is in a Dojo challenge their SkyBlock Menu slot holds a Cancel challenge item instead.
 * The Crimson Isle server registers what cancelling does; generic menu code only asks whether a lock is held.
 */
public final class DojoChallengeLock {
    public static final String MENU_BLOCKED = "<c>You can't open the SkyBlock Menu in a challenge!";
    private static final Map<UUID, Consumer<SkyBlockPlayer>> LOCKS = new ConcurrentHashMap<>();

    private DojoChallengeLock() {
    }

    public static void lock(SkyBlockPlayer player, Consumer<SkyBlockPlayer> onCancel) {
        LOCKS.put(player.getUuid(), onCancel);
    }

    public static void unlock(SkyBlockPlayer player) {
        LOCKS.remove(player.getUuid());
    }

    public static boolean isLocked(SkyBlockPlayer player) {
        return LOCKS.containsKey(player.getUuid());
    }

    public static void cancel(SkyBlockPlayer player) {
        Consumer<SkyBlockPlayer> onCancel = LOCKS.get(player.getUuid());
        if (onCancel != null) onCancel.accept(player);
    }

    public static ItemStack cancelItem() {
        return ItemStacks.notEditable(ItemStacks.item(Material.BARRIER, """
                <c>Cancel challenge
                <7>Leave the Arena and end the challenge.
                <7>Cancelling counts as a <c>fail<7>.

                <e>Click to cancel!""")).build();
    }
}
