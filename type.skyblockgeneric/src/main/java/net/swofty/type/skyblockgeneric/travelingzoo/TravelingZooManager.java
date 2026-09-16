package net.swofty.type.skyblockgeneric.travelingzoo;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Determines Oringo the Traveling Zookeeper's current visit offer (Roadmap #21).
//
// Real Hypixel (SkyBlock Wiki, "Traveling Zoo"): Oringo sells exactly 3 of 6 possible pets per
// visit - one guaranteed-Legendary pet chosen from a fixed yearly-rotating sequence
// (Lion -> Monkey -> Elephant -> Giraffe -> Blue Whale -> Tiger), plus 2 random pets at a random
// rarity between Common and Epic. Pricing is coins PLUS a species-specific raw/enchanted material
// (cod for Blue Whale, beef for Lion, chicken for Tiger, logs for the rest), scaling steeply with
// rarity (documented range: 10,000 coins + 64 raw material at Common, up to 10-18,000,000 coins +
// 512-1,024 enchanted material at Legendary).
//
// The exact per-rarity cost table isn't wiki-documented to the coin, so the figures below are a
// reasonable interpolation within that documented range, not a sourced 1:1 copy - flagged here
// rather than silently presented as exact.
public class TravelingZooManager {

    private static final List<ItemType> LEGENDARY_ROTATION = List.of(
            ItemType.LION_PET,
            ItemType.MONKEY_PET,
            ItemType.ELEPHANT_PET,
            ItemType.GIRAFFE_PET,
            ItemType.BLUE_WHALE_PET,
            ItemType.TIGER_PET
    );

    private static final Rarity[] RANDOM_RARITIES = {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC};

    public record PetOffer(ItemType petType, Rarity rarity, ShopPrice price) {
    }

    // Deterministic per SkyBlock year so the offer doesn't reshuffle every time a player reopens
    // the shop mid-visit, matching a real "3 pets for this visit" stock rather than a live reroll.
    public static List<PetOffer> getCurrentOffers() {
        int year = Math.max(1, SkyBlockCalendar.getYear());
        int rotationIndex = (year - 1) % LEGENDARY_ROTATION.size();
        ItemType legendaryPet = LEGENDARY_ROTATION.get(rotationIndex);

        List<ItemType> remaining = new ArrayList<>(LEGENDARY_ROTATION);
        remaining.remove(legendaryPet);
        Collections.shuffle(remaining, new Random(year));
        List<ItemType> randomPets = remaining.subList(0, 2);

        Random rarityRoll = new Random((long) year * 31);
        List<PetOffer> offers = new ArrayList<>();
        offers.add(new PetOffer(legendaryPet, Rarity.LEGENDARY, priceFor(legendaryPet, Rarity.LEGENDARY)));
        for (ItemType pet : randomPets) {
            Rarity rarity = RANDOM_RARITIES[rarityRoll.nextInt(RANDOM_RARITIES.length)];
            offers.add(new PetOffer(pet, rarity, priceFor(pet, rarity)));
        }
        return offers;
    }

    private static ItemType rawMaterialFor(ItemType pet) {
        if (pet == ItemType.LION_PET) return ItemType.RAW_BEEF;
        if (pet == ItemType.TIGER_PET) return ItemType.RAW_CHICKEN;
        if (pet == ItemType.BLUE_WHALE_PET) return ItemType.RAW_FISH;
        return ItemType.OAK_LOG;
    }

    private static ItemType enchantedMaterialFor(ItemType pet) {
        if (pet == ItemType.LION_PET) return ItemType.ENCHANTED_RAW_BEEF;
        if (pet == ItemType.TIGER_PET) return ItemType.ENCHANTED_RAW_CHICKEN;
        if (pet == ItemType.BLUE_WHALE_PET) return ItemType.ENCHANTED_RAW_FISH;
        return ItemType.ENCHANTED_OAK_LOG;
    }

    private static ShopPrice priceFor(ItemType pet, Rarity rarity) {
        long coins;
        int amount;
        ItemType material;
        switch (rarity) {
            case COMMON -> {
                coins = 10_000;
                amount = 64;
                material = rawMaterialFor(pet);
            }
            case UNCOMMON -> {
                coins = 50_000;
                amount = 128;
                material = rawMaterialFor(pet);
            }
            case RARE -> {
                coins = 500_000;
                amount = 256;
                material = rawMaterialFor(pet);
            }
            case EPIC -> {
                coins = 2_000_000;
                amount = 384;
                material = enchantedMaterialFor(pet);
            }
            default -> {
                coins = 14_000_000;
                amount = 768;
                material = enchantedMaterialFor(pet);
            }
        }
        return new CombinedShopPrice(List.of(
                new CoinShopPrice(coins),
                new ItemShopPrice(material, amount)
        ));
    }
}
