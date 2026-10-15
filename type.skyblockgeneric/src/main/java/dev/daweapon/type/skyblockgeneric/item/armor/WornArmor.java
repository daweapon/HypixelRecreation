package dev.daweapon.type.skyblockgeneric.item.armor;

import net.minestom.server.item.ItemStack;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WornArmor {
    private static final long CACHE_MILLIS = 500;
    private static final Map<UUID, CachedIds> CACHE = new ConcurrentHashMap<>();

    private record CachedIds(List<String> ids, long computedAt) {
    }

    private WornArmor() {
    }

    public static List<String> ids(SkyBlockPlayer player) {
        return List.of(id(player.getHelmet()), id(player.getChestplate()), id(player.getLeggings()), id(player.getBoots()));
    }

    public static List<String> cachedIds(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        CachedIds cached = CACHE.get(player.getUuid());
        if (cached != null && now - cached.computedAt() < CACHE_MILLIS) return cached.ids();
        List<String> ids = ids(player);
        CACHE.put(player.getUuid(), new CachedIds(ids, now));
        return ids;
    }

    public static String id(ItemStack stack) {
        if (stack == null || stack.isAir()) return "";
        String id = new SkyBlockItem(stack).getAttributeHandler().getTypeAsString();
        return id == null ? "" : id;
    }

    public static boolean wearsAll(List<String> worn, Set<String> pieces) {
        return worn.containsAll(pieces);
    }

    public static int count(List<String> worn, Set<String> pieces) {
        int count = 0;
        for (String id : worn) if (pieces.contains(id)) count++;
        return count;
    }
}
