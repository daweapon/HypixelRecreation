package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.OwnedAccessory;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DanteAccess {
    public static final long HAPPY_MASK_PRICE = 100;
    public static final long TALISMAN_PRICE = 1_000_000;

    private static final long CACHE_MILLIS = 1500;
    private static final Set<ItemType> PASS_ACCESSORIES = Set.of(ItemType.DANTE_TALISMAN, ItemType.DANTE_RING);
    private static final Map<UUID, Cached> CACHE = new ConcurrentHashMap<>();

    private record Cached(boolean allowed, long at) {
    }

    private DanteAccess() {
    }

    public static boolean securityActive() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.INCREASED_SECURITY);
    }

    public static boolean mayorIsDante() {
        return dev.daweapon.type.skyblockgeneric.elections.ElectionManager.getCurrentMayor() == SkyBlockMayor.DANTE;
    }

    public static boolean grants(boolean wearsMask, Collection<ItemType> accessories, boolean coopMemberHasTalisman) {
        return wearsMask || coopMemberHasTalisman || accessories.stream().anyMatch(PASS_ACCESSORIES::contains);
    }

    public static boolean canEnter(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        Cached cached = CACHE.get(player.getUuid());
        if (cached != null && now - cached.at() < CACHE_MILLIS) return cached.allowed();
        boolean allowed = grants(wearsMask(player), accessoryTypes(player), coopMemberHasTalisman(player));
        CACHE.put(player.getUuid(), new Cached(allowed, now));
        return allowed;
    }

    public static void forget(SkyBlockPlayer player) {
        CACHE.remove(player.getUuid());
    }

    private static boolean wearsMask(SkyBlockPlayer player) {
        ItemStack helmet = player.getHelmet();
        if (helmet.isAir() || !SkyBlockItem.isSkyBlockItem(helmet)) return false;
        return new SkyBlockItem(helmet).getAttributeHandler().getPotentialType() == ItemType.HAPPY_MASK;
    }

    private static List<ItemType> accessoryTypes(SkyBlockPlayer player) {
        return AccessoryOwnership.collect(player).stream().map(OwnedAccessory::type).toList();
    }

    private static boolean coopMemberHasTalisman(SkyBlockPlayer player) {
        if (!player.isCoop()) return false;
        for (SkyBlockPlayer member : player.getCoop().getOnlineMembers()) {
            if (member != player && accessoryTypes(member).contains(ItemType.DANTE_TALISMAN)) return true;
        }
        return false;
    }
}
