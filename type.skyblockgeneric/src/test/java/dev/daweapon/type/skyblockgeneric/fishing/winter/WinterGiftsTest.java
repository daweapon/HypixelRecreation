package dev.daweapon.type.skyblockgeneric.fishing.winter;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.gifts.PartyGift;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinterGiftsTest {

    private static double share(List<WinterGifts.GiftWeight> gifts, ItemType item) {
        int total = gifts.stream().mapToInt(WinterGifts.GiftWeight::weight).sum();
        int weight = gifts.stream().filter(g -> g.item() == item).mapToInt(WinterGifts.GiftWeight::weight).sum();
        return Math.round(weight * 10_000D / total) / 100D;
    }

    @Test
    void reindrakeGiftsSplitSixtyFiveTwentyFiveTen() {
        List<WinterGifts.GiftWeight> gifts = WinterGifts.reindrakeGifts(false);
        assertEquals(65D, share(gifts, ItemType.WHITE_GIFT));
        assertEquals(25D, share(gifts, ItemType.GREEN_GIFT));
        assertEquals(10D, share(gifts, ItemType.RED_GIFT));
        assertFalse(gifts.stream().anyMatch(g -> g.item() == ItemType.PARTY_GIFT));
    }

    @Test
    void timeForGivingAddsPartyGiftsAtTheWikiSplit() {
        List<WinterGifts.GiftWeight> gifts = WinterGifts.reindrakeGifts(true);
        assertEquals(61.9D, share(gifts, ItemType.WHITE_GIFT), 0.05);
        assertEquals(23.81D, share(gifts, ItemType.GREEN_GIFT));
        assertEquals(9.52D, share(gifts, ItemType.RED_GIFT));
        assertEquals(4.76D, share(gifts, ItemType.PARTY_GIFT));
    }

    @Test
    void reindrakeHitsDropAGiftOneTimeInTen() {
        Random random = new Random(7);
        int hits = 100_000;
        int gifts = 0;
        for (int i = 0; i < hits; i++) {
            if (WinterGifts.rollReindrakeHit(null, false, random) != null) gifts++;
        }
        assertEquals(0.1, gifts / (double) hits, 0.005);
    }

    @Test
    void partyGiftChanceDoublesWithEachRarity() {
        assertEquals(2D, WinterGifts.partyGiftChance(Rarity.COMMON));
        assertEquals(5D, WinterGifts.partyGiftChance(Rarity.UNCOMMON));
        assertEquals(10D, WinterGifts.partyGiftChance(Rarity.RARE));
        assertEquals(20D, WinterGifts.partyGiftChance(Rarity.EPIC));
        assertEquals(40D, WinterGifts.partyGiftChance(Rarity.LEGENDARY));
        assertEquals(0D, WinterGifts.partyGiftChance(Rarity.MYTHIC));
    }

    @Test
    void partyGiftTableMatchesTheWikiTotal() {
        assertEquals(1_674, PartyGift.REWARDS.stream().mapToInt(PartyGift.Reward::weight).sum());
        assertTrue(PartyGift.REWARDS.stream()
                .filter(reward -> reward.tier() == PartyGift.Tier.PARTY_TIER && reward.coins() == 0)
                .allMatch(reward -> reward.item() != null));
    }

    @Test
    void everyWinterCreatureButTheReindrakePaysIceEssence() {
        for (WinterSeaCreature creature : WinterSeaCreature.values()) {
            boolean paysEssence = creature.getMaxIceEssence() > 0;
            assertEquals(creature != WinterSeaCreature.REINDRAKE, paysEssence, creature.name());
        }
        assertEquals(100, WinterSeaCreature.YETI.getMinIceEssence());
    }
}
