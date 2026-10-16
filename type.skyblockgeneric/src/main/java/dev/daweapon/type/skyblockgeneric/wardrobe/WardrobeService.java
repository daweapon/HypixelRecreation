package dev.daweapon.type.skyblockgeneric.wardrobe;

import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgradeId;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgrades;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointWardrobe;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class WardrobeService {

    public static int rankSlots(Rank rank) {
        if (rank.isEqualOrHigherThan(Rank.MVP_PLUS)) return 18;
        if (rank.isEqualOrHigherThan(Rank.MVP)) return 13;
        if (rank.isEqualOrHigherThan(Rank.VIP_PLUS)) return 9;
        if (rank.isEqualOrHigherThan(Rank.VIP)) return 5;
        return 2;
    }

    public static boolean isUnlocked(int slot, SkyBlockPlayer player) {
        return slot < Math.min(27, rankSlots(player.getRank()) + CommunityUpgrades.intValue(player, CommunityUpgradeId.ARMOR_WARDROBE));
    }

    public static boolean accepts(int pieceIndex, SkyBlockItem item) {
        if (item == null || item.isNA()) return true;
        Material material = item.getMaterial();
        String key = material.key().value();
        return switch (pieceIndex) {
            case 0 -> key.endsWith("_helmet") || material == Material.PLAYER_HEAD;
            case 1 -> key.endsWith("_chestplate") || material == Material.ELYTRA;
            case 2 -> key.endsWith("_leggings");
            case 3 -> key.endsWith("_boots");
            default -> false;
        };
    }
}
