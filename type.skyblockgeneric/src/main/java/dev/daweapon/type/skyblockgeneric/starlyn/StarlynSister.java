package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import lombok.Getter;

import java.util.List;

@Getter
public enum StarlynSister {
    AGATHA("Agatha", "<2>Moonglade Marsh", "f50b136168f3708c0cca6f42d140465c3a1a447a2e425ec45e788c5c7920d365",
            HotfCurrency.FOREST, ItemType.AGATHA_COUPON, ItemType.STARLYN_PRIZE,
            List.of(CustomCollectionAward.AGATHA_MILESTONE_4_FORAGING_LEVEL_CAP, CustomCollectionAward.AGATHA_MILESTONE_5_FORAGING_LEVEL_CAP)),
    MIRIA("Miria", "<6>Torrhus Canyon", "388ab9514c11cbe0e8c39d63387e8cc6c1e94594f1dc2bd622789c73d6795d3d",
            HotfCurrency.DESERT, ItemType.MIRIA_COUPON, ItemType.MIRIA_PRIZE,
            List.of(CustomCollectionAward.MIRIA_MILESTONE_4_FORAGING_LEVEL_CAP, CustomCollectionAward.MIRIA_MILESTONE_5_FORAGING_LEVEL_CAP));

    private final String displayName;
    private final String island;
    private final String headTexture;
    private final HotfCurrency currency;
    private final ItemType coupon;
    private final ItemType prize;
    private final List<CustomCollectionAward> levelCapAwards;

    StarlynSister(String displayName, String island, String headTexture, HotfCurrency currency, ItemType coupon,
                  ItemType prize, List<CustomCollectionAward> levelCapAwards) {
        this.displayName = displayName;
        this.island = island;
        this.headTexture = headTexture;
        this.currency = currency;
        this.coupon = coupon;
        this.prize = prize;
        this.levelCapAwards = levelCapAwards;
    }

    public String key() {
        return name().toLowerCase();
    }

    public String trigger(String suffix) {
        return key() + "." + suffix;
    }

    public String npcName() {
        return "<b>" + displayName;
    }

    public String couponName() {
        return displayName + "'s Coupon";
    }

    public String prizeName() {
        return displayName + "'s Prize";
    }

    public String contestName() {
        return displayName + "'s Contest";
    }

    public String shopName() {
        return displayName + "'s Shop";
    }

    public String milestoneName() {
        return displayName + "'s Milestone";
    }

    public static StarlynSister fromKey(String key) {
        return valueOf(key.toUpperCase());
    }
}
