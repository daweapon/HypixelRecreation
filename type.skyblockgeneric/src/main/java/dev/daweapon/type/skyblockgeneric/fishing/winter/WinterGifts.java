package dev.daweapon.type.skyblockgeneric.fishing.winter;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Gifts handed out by Winter Sea Creatures. A Reindrake drops a Gift on 1 in 10 hits, picked
 * White 65 / Green 25 / Red 10; Foxy's "A Time for Giving" perk adds a Party Gift at weight 5,
 * which gives the wiki's 61.94 / 23.81 / 9.52 / 4.76 split. The same perk gives every other
 * Winter Sea Creature a Party Gift chance by rarity (wiki: Party Gift).
 */
public final class WinterGifts {
    public static final double REINDRAKE_GIFT_CHANCE = 10;

    private WinterGifts() {
    }

    public record GiftWeight(ItemType item, int weight) {
    }

    public static boolean timeForGiving() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.A_TIME_FOR_GIVING);
    }

    public static List<GiftWeight> reindrakeGifts(boolean timeForGiving) {
        List<GiftWeight> gifts = new ArrayList<>(List.of(
                new GiftWeight(ItemType.WHITE_GIFT, 65),
                new GiftWeight(ItemType.GREEN_GIFT, 25),
                new GiftWeight(ItemType.RED_GIFT, 10)));
        if (timeForGiving) gifts.add(new GiftWeight(ItemType.PARTY_GIFT, 5));
        return gifts;
    }

    public static @Nullable ItemType rollReindrakeHit(@Nullable UUID player, boolean timeForGiving, RandomGenerator random) {
        List<GiftWeight> gifts = reindrakeGifts(timeForGiving);
        ItemType forced = LootRoll.claim(player, gifts.stream().map(GiftWeight::item).toList());
        if (forced != null) return forced;
        if (!LootRoll.roll(null, null, REINDRAKE_GIFT_CHANCE, random)) return null;
        return pickGift(gifts, random);
    }

    public static ItemType pickGift(List<GiftWeight> gifts, RandomGenerator random) {
        return LootRoll.pick(null, gifts, GiftWeight::item, GiftWeight::weight, random).item();
    }

    public static double partyGiftChance(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 2;
            case UNCOMMON -> 5;
            case RARE -> 10;
            case EPIC -> 20;
            case LEGENDARY -> 40;
            default -> 0;
        };
    }
}
