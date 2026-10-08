package net.swofty.type.skyblockgeneric.mythological.gear;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;

/**
 * Coins from the Mythological Ritual: burrow coins (Crown of Greed, Deadly Greed, King of Greed,
 * the Four-Eyed Fish) and Mythological mob coins (Mythological Greed), both of which a worn Crown
 * of Avarice eats instead.
 */
public final class MythologicalCoins {
    private static final int CROWN_OF_GREED_MULTIPLIER = 2;
    private static final int FOUR_EYED_FISH_COINS = 2_000;

    private MythologicalCoins() {
    }

    public static long fromBurrow(SkyBlockPlayer player, long baseCoins) {
        double coins = baseCoins * Carnivals.multiplier(player, CarnivalPerk.DEADLY_GREED)
                * (1 + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("E42")) / 100D);
        if (CrownOfAvarice.isWorn(player)) return CrownOfAvarice.feed(player, Math.round(coins));
        if (isWearing(player, ItemType.CROWN_OF_GREED)) coins *= CROWN_OF_GREED_MULTIPLIER;
        return Math.round(coins);
    }

    public static long fromMob(SkyBlockPlayer player, long baseCoins) {
        long coins = Math.round(baseCoins * MythologicalGear.mobCoinMultiplier(player));
        return CrownOfAvarice.isWorn(player) ? CrownOfAvarice.feed(player, coins) : coins;
    }

    public static void onBurrowDug(SkyBlockPlayer player) {
        if (PetItems.heldType(player.getPetData().getEnabledPet()) != ItemType.FOUR_EYED_FISH) return;
        long coins = fromBurrow(player, FOUR_EYED_FISH_COINS);
        if (coins > 0) player.addCoins(coins);
    }

    private static boolean isWearing(SkyBlockPlayer player, ItemType helmet) {
        if (player.getHelmet().isAir()) return false;
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == helmet;
    }
}
