package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentRefrigerate;
import net.swofty.type.skyblockgeneric.loottable.ForcedDrops;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiftTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void weightsMatchTheWikiTotals() {
        assertEquals(6649, Gift.WHITE.totalWeight());
        assertEquals(6503, Gift.GREEN.totalWeight());
        assertEquals(3876, Gift.RED.totalWeight());
    }

    @Test
    void everyPickIsAValidRewardFromItsOwnTable() {
        Random random = new Random(7);
        for (Gift gift : Gift.values()) {
            for (int i = 0; i < 5_000; i++) {
                GiftReward reward = gift.pick(null, 1, random);
                assertTrue(gift.getRewards().contains(reward));
                assertTrue(reward.amount() > 0);
                if (reward.kind() == GiftReward.Kind.ITEM) assertNotNull(reward.item());
            }
        }
    }

    @Test
    void commonRewardsDominateAndSantaRewardsAreRare() {
        Random random = new Random(11);
        int santa = 0;
        int rolls = 50_000;
        for (int i = 0; i < rolls; i++) {
            if (Gift.WHITE.pick(null, 1, random).rarity() == GiftDropRarity.SANTA) santa++;
        }
        assertTrue(santa < rolls * 0.002, "White Gift SANTA drops should be about 0.06%, got " + santa);
    }

    @Test
    void winterSerendipityOnlyRaisesRareItemWeights() {
        GiftReward coins = Gift.WHITE.getRewards().getFirst();
        GiftReward fish = Gift.WHITE.getRewards().stream().filter(r -> r.item() == ItemType.GIFT_THE_FISH).findFirst().orElseThrow();
        assertEquals(coins.weight(), coins.weight(1.2), 1e-9);
        assertEquals(fish.weight() * 1.2, fish.weight(1.2), 1e-9);
    }

    @Test
    void aForcedItemIsPickedFromTheGift() {
        UUID player = UUID.randomUUID();
        ForcedDrops.force(player, ItemType.SNOWMAN_PET);
        assertSame(ItemType.SNOWMAN_PET, Gift.RED.pick(player, 1, new Random(3)).item());
    }

    @Test
    void hollyDyeOnlyRollsFromRedGifts() {
        UUID player = UUID.randomUUID();
        ForcedDrops.force(player, ItemType.DYE_HOLLY);
        assertTrue(!Gift.WHITE.rollHollyDye(player, new Random(1)));
        assertTrue(Gift.RED.rollHollyDye(player, new Random(1)));
    }

    @Test
    void northStarsStayWithinTheGiftRange() {
        Random random = new Random(5);
        for (int i = 0; i < 2_000; i++) {
            int stars = Gift.RED.rollNorthStars(random);
            assertTrue(stars >= 0 && stars <= 5);
            assertTrue(Gift.WHITE.rollNorthStars(random) <= 1);
        }
    }

    @Test
    void krampusHelmetGainsOneHealthPerFiftyGiftsUpToFiveHundred() {
        assertEquals(0, SnowSuit.krampusHealth(49));
        assertEquals(2, SnowSuit.krampusHealth(100));
        assertEquals(500, SnowSuit.krampusHealth(1_000_000));
    }

    @Test
    void refrigerateCapsDefensePerPiece() {
        assertEquals(30, EnchantmentRefrigerate.defenseGained(Map.of(1, 1), 1000), 1e-9);
        assertEquals(600, EnchantmentRefrigerate.defenseGained(Map.of(5, 4), 100_000), 1e-9);
    }
}
