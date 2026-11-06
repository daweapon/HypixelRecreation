package dev.daweapon.type.catacombs.secret;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Superboom TNT and InfiniBoom TNT in a run: either click with one on a crackable wall or a crypt blows it open. A
 * Superboom TNT is used up; the InfiniBoom keeps its 2 second cooldown instead.
 */
public final class SuperboomTnt {
    public static final long INFINITE_COOLDOWN_MILLIS = 2_000;

    private final Map<UUID, Long> lastInfinite = new ConcurrentHashMap<>();

    public static boolean isSuperboom(ItemStack stack) {
        return type(stack) != null;
    }

    private static ItemType type(ItemStack stack) {
        if (stack == null || stack.isAir()) return null;
        ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
        return type == ItemType.SUPERBOOM_TNT || type == ItemType.INFINITE_SUPERBOOM_TNT ? type : null;
    }

    /** Whether the player's held Superboom may go off now. */
    public boolean ready(SkyBlockPlayer player) {
        if (type(player.getItemInMainHand()) != ItemType.INFINITE_SUPERBOOM_TNT) return true;
        return System.currentTimeMillis() - lastInfinite.getOrDefault(player.getUuid(), 0L) >= INFINITE_COOLDOWN_MILLIS;
    }

    /** Uses up one Superboom TNT, or starts the InfiniBoom's cooldown. */
    public void spend(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        ItemType type = type(held);
        if (type == ItemType.INFINITE_SUPERBOOM_TNT) {
            lastInfinite.put(player.getUuid(), System.currentTimeMillis());
            return;
        }
        if (type == ItemType.SUPERBOOM_TNT) player.setItemInHand(PlayerHand.MAIN, held.amount() > 1 ? held.withAmount(held.amount() - 1) : ItemStack.AIR);
    }
}
