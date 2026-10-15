package dev.daweapon.type.hub.gui.lucius;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public enum LuciusTier {
    SHADY_RING(ItemType.SHADY_RING, 1, 500_000, null),
    CROOKED_ARTIFACT(ItemType.CROOKED_ARTIFACT, 3, 2_000_000, ItemType.SHADY_RING),
    SEAL_OF_THE_FAMILY(ItemType.SEAL_OF_THE_FAMILY, 7, 10_000_000, ItemType.CROOKED_ARTIFACT),
    ;

    private final ItemType reward;
    private final int requiredPurchases;
    private final double coinCost;
    private final @Nullable ItemType tradedIn;

    public static int darkAuctionPurchases(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.DARK_AUCTION_PURCHASES, DatapointInteger.class).getValue();
    }

    public static DatapointInteger purchasedTiers(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.LUCIUS_TIERS_PURCHASED, DatapointInteger.class);
    }

    public static @Nullable LuciusTier next(SkyBlockPlayer player) {
        int purchased = purchasedTiers(player).getValue();
        return purchased < values().length ? values()[purchased] : null;
    }
}
