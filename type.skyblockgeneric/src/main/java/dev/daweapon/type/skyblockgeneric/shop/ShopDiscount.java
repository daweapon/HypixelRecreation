package dev.daweapon.type.skyblockgeneric.shop;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.elections.perks.TaxPerks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class ShopDiscount {
    private record Source(ItemType accessory, double fraction) {
    }

    private static final List<Source> SOURCES = List.of(
            new Source(ItemType.SEAL_OF_THE_FAMILY, 0.03),
            new Source(ItemType.CROOKED_ARTIFACT, 0.02),
            new Source(ItemType.SHADY_RING, 0.01)
    );

    private ShopDiscount() {
    }

    public static double fraction(SkyBlockPlayer player) {
        List<ItemType> owned = AccessoryOwnership.ownedTypes(player);
        return SOURCES.stream()
                .filter(source -> owned.contains(source.accessory()))
                .mapToDouble(Source::fraction)
                .findFirst()
                .orElse(0);
    }

    public static double apply(SkyBlockPlayer player, double coins) {
        return coins * (1 - fraction(player)) * TaxPerks.npcShopMultiplier();
    }
}
