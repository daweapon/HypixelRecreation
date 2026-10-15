package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class KuudraCollectionTest {

    @Test
    void clearsCountByTier() {
        Map<KuudraTier, Long> clears = new EnumMap<>(KuudraTier.class);
        clears.put(KuudraTier.BASIC, 3L);
        clears.put(KuudraTier.INFERNAL, 2L);
        assertEquals(13, KuudraProgress.collectionPoints(clears));
    }

    @Test
    void tiersAndSkyBlockXPFollowThePoints() {
        assertNull(KuudraCollectionTier.highestReached(9));
        assertEquals(KuudraCollectionTier.I, KuudraCollectionTier.highestReached(10));
        assertEquals(KuudraCollectionTier.IV, KuudraCollectionTier.highestReached(4_999));
        assertEquals(5, KuudraCollectionTier.reachedCount(5_000));
        assertEquals(10, KuudraCollectionTier.skyBlockXP(10));
        assertEquals(100, KuudraCollectionTier.skyBlockXP(1_000_000));
    }

    @Test
    void rewardItemsExist() {
        for (KuudraCollectionTier tier : KuudraCollectionTier.values()) assertNotNull(ItemType.get(tier.getItemId()), tier.name());
        assertNotNull(ItemType.TENTACLE_DYE);
    }
}
