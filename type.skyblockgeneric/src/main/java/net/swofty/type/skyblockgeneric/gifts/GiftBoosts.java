package net.swofty.type.skyblockgeneric.gifts;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.CarnivalStatistics;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class GiftBoosts {
    private static final Map<ItemType, Double> GIFT_TALISMANS = Map.of(
            ItemType.WHITE_GIFT_TALISMAN, 5D, ItemType.GREEN_GIFT_TALISMAN, 10D, ItemType.BLUE_GIFT_TALISMAN, 15D,
            ItemType.PURPLE_GIFT_TALISMAN, 20D, ItemType.GOLD_GIFT_TALISMAN, 25D);
    private static final double FEELIN_FESTIVE_PERCENT = 10;
    private static final double SNOW_SUIT_GIFT_ATTACK_PERCENT_PER_PIECE = 5;
    private static final AttributeId WINTERS_SERENDIPITY = AttributeId.parse("R35");

    private GiftBoosts() {
    }

    public static double coinAndXpMultiplier(SkyBlockPlayer player) {
        double percent = talismanPercent(player)
                + Carnivals.perkValue(player, CarnivalPerk.PROFESSIONAL_GIFTER)
                + (CarnivalStatistics.isWearing(player, ItemType.SNOWMAN_MASK) ? FEELIN_FESTIVE_PERCENT : 0);
        return 1 + percent / 100D;
    }

    public static double rareItemMultiplier(SkyBlockPlayer player) {
        return 1 + AttributeEffectService.value(player.getHuntingData(), WINTERS_SERENDIPITY) / 100D;
    }

    public static double giftAttackBonusPercent(SkyBlockPlayer player) {
        return SnowSuit.wornPieces(player) * SNOW_SUIT_GIFT_ATTACK_PERCENT_PER_PIECE + ReindeerPet.snowPowerPercent(player);
    }

    public static int withGiftAttackBonus(SkyBlockPlayer player, ItemType gift, int earned) {
        double chance = giftAttackBonusPercent(player);
        int total = earned;
        for (int i = 0; i < earned; i++) {
            if (LootRoll.roll(player.getUuid(), gift, chance)) total++;
        }
        return total;
    }

    public static double talismanPercent(SkyBlockPlayer player) {
        double best = 0;
        for (SkyBlockItem accessory : player.getAccessoryBag().getUniqueAccessories()) {
            best = Math.max(best, GIFT_TALISMANS.getOrDefault(accessory.getAttributeHandler().getPotentialType(), 0D));
        }
        return best;
    }
}
