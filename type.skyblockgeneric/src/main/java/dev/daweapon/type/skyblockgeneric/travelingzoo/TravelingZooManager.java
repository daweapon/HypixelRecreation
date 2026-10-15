package dev.daweapon.type.skyblockgeneric.travelingzoo;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Determines Oringo the Traveling Zookeeper's current visit offer (Roadmap #21).
//
// Real Hypixel (SkyBlock Wiki, "Traveling Zoo"): Oringo sells exactly 3 of 6 possible pets per
// visit - one guaranteed-Legendary pet chosen from a fixed yearly-rotating sequence
// (Lion -> Monkey -> Elephant -> Giraffe -> Blue Whale -> Tiger), plus 2 random pets at a random
// rarity between Common and Epic. Pricing is coins PLUS a species-specific raw/enchanted material
// (cod for Blue Whale, beef for Lion, chicken for Tiger, acacia, jungle and dark oak logs for
// Giraffe, Monkey and Elephant), per the wiki's Oringo price table.
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

    private record Cost(long coins, ItemType material, int amount) {
    }

    // Oringo's prices per rarity, Common to Legendary.
    private static final Map<ItemType, List<Cost>> PRICES = Map.of(
            ItemType.LION_PET, List.of(
                    new Cost(10_000, ItemType.RAW_BEEF, 64),
                    new Cost(25_000, ItemType.ENCHANTED_RAW_BEEF, 2),
                    new Cost(100_000, ItemType.ENCHANTED_RAW_BEEF, 32),
                    new Cost(1_000_000, ItemType.ENCHANTED_RAW_BEEF, 256),
                    new Cost(15_000_000, ItemType.ENCHANTED_RAW_BEEF, 1024)),
            ItemType.TIGER_PET, List.of(
                    new Cost(10_000, ItemType.RAW_CHICKEN, 128),
                    new Cost(25_000, ItemType.ENCHANTED_RAW_CHICKEN, 2),
                    new Cost(100_000, ItemType.ENCHANTED_RAW_CHICKEN, 32),
                    new Cost(1_000_000, ItemType.ENCHANTED_RAW_CHICKEN, 256),
                    new Cost(15_000_000, ItemType.ENCHANTED_RAW_CHICKEN, 1024)),
            ItemType.GIRAFFE_PET, List.of(
                    new Cost(10_000, ItemType.ACACIA_LOG, 64),
                    new Cost(25_000, ItemType.ENCHANTED_ACACIA_LOG, 1),
                    new Cost(100_000, ItemType.ENCHANTED_ACACIA_LOG, 16),
                    new Cost(1_000_000, ItemType.ENCHANTED_ACACIA_LOG, 128),
                    new Cost(10_000_000, ItemType.ENCHANTED_ACACIA_LOG, 512)),
            ItemType.MONKEY_PET, List.of(
                    new Cost(10_000, ItemType.JUNGLE_LOG, 64),
                    new Cost(25_000, ItemType.ENCHANTED_JUNGLE_LOG, 1),
                    new Cost(100_000, ItemType.ENCHANTED_JUNGLE_LOG, 16),
                    new Cost(1_000_000, ItemType.ENCHANTED_JUNGLE_LOG, 128),
                    new Cost(18_000_000, ItemType.ENCHANTED_JUNGLE_LOG, 512)),
            ItemType.ELEPHANT_PET, List.of(
                    new Cost(10_000, ItemType.DARK_OAK_LOG, 64),
                    new Cost(25_000, ItemType.ENCHANTED_DARK_OAK_LOG, 1),
                    new Cost(100_000, ItemType.ENCHANTED_DARK_OAK_LOG, 16),
                    new Cost(1_000_000, ItemType.ENCHANTED_DARK_OAK_LOG, 128),
                    new Cost(15_000_000, ItemType.ENCHANTED_DARK_OAK_LOG, 512)),
            ItemType.BLUE_WHALE_PET, List.of(
                    new Cost(10_000, ItemType.RAW_FISH, 64),
                    new Cost(25_000, ItemType.ENCHANTED_RAW_FISH, 1),
                    new Cost(100_000, ItemType.ENCHANTED_RAW_FISH, 16),
                    new Cost(1_000_000, ItemType.ENCHANTED_COOKED_FISH, 1),
                    new Cost(10_000_000, ItemType.ENCHANTED_COOKED_FISH, 8))
    );

    private static ShopPrice priceFor(ItemType pet, Rarity rarity) {
        Cost cost = PRICES.get(pet).get(Math.min(rarity.ordinal(), 4));
        return new CombinedShopPrice(List.of(
                new CoinShopPrice(cost.coins()),
                new ItemShopPrice(cost.material(), cost.amount())
        ));
    }
}
