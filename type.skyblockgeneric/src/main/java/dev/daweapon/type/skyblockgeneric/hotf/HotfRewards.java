package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HotfRewards {
    public static final int DAILY_GIFT_PERKS = 250;
    public static final int BASE_GIFT_WHISPERS_PER_LOG = 1;
    private static final int CORE_GIFT_WHISPERS = 20;
    private static final int CORE_LOG_WHISPERS = 2;
    private static final int TREE_WHISPERER_WHISPERS = 200;
    private static final int GIFT_PERK_WHISPERS = 20;

    private HotfRewards() {
    }

    public static boolean coreBonusesActive(SkyBlockPlayer player) {
        return HotfService.data(player).getCoreLevel() >= 3;
    }

    public static void onLogCut(SkyBlockPlayer player, ItemType logType) {
        if (!HotfService.onForagingIsland()) return;
        if (logType != ItemType.FIG_LOG && logType != ItemType.MANGROVE_LOG) return;
        long base = BASE_GIFT_WHISPERS_PER_LOG + (coreBonusesActive(player) ? CORE_LOG_WHISPERS : 0);
        addBoosted(player, HotfCurrency.FOREST, base);
        claimDailyWish(player);
    }

    private static void claimDailyWish(SkyBlockPlayer player) {
        double amount = HotfService.perkValue(player, "daily_wishes");
        if (amount <= 0) return;
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.dailyDay();
        if (data.getWishDay() == today) return;
        data.setWishDay(today);
        HotfService.save(player, data);
        addBoosted(player, HotfCurrency.FOREST, Math.round(amount));
        player.sendMessage("<a>Daily Wishes<7> granted <3>+{} Forest Whispers<7>!", String.format("%,d", Math.round(amount)));
    }

    public static long giftWhispers(SkyBlockPlayer player, HotfCurrency currency, long base) {
        long total = base;
        if (coreBonusesActive(player)) total += CORE_GIFT_WHISPERS;
        if (HotfService.hasPerk(player, "tree_whisperer")) total += TREE_WHISPERER_WHISPERS;
        if (HotfService.hasPerk(player, "gifts_250") && giftsToday(player) < DAILY_GIFT_PERKS) total += GIFT_PERK_WHISPERS;
        return addBoosted(player, currency, total);
    }

    public static double lootBonusPercent(SkyBlockPlayer player) {
        double bonus = HotfService.perkValue(player, "luck_of_the_forest");
        if (giftsToday(player) < DAILY_GIFT_PERKS) bonus += HotfService.perkValue(player, "gifts_250");
        return bonus;
    }

    public static int giftsToday(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        return data.getGiftsDay() == HotfService.dailyDay() ? data.getGiftsToday() : 0;
    }

    public static void recordGift(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.dailyDay();
        if (data.getGiftsDay() != today) {
            data.setGiftsDay(today);
            data.setGiftsToday(0);
        }
        data.setGiftsToday(data.getGiftsToday() + 1);
        HotfService.save(player, data);
    }

    public static void recordTree(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.dailyDay();
        if (data.getTreesDay() != today) {
            data.setTreesDay(today);
            data.setTreesToday(0);
        }
        data.setTreesToday(data.getTreesToday() + 1);
        HotfService.save(player, data);
    }

    public static boolean timberFells(SkyBlockPlayer player) {
        double chance = player.getStatistics().allStatistics().getOverall(ItemStatistic.TIMBER);
        return chance > 0 && LootRoll.roll(player.getUuid(), null, Math.min(100, chance));
    }

    public static boolean claimFreeTrial(SkyBlockPlayer player) {
        if (!HotfService.hasPerk(player, "free_trial")) return false;
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        long today = HotfService.dailyDay();
        if (data.getFreeTrialDay() == today) return false;
        ItemType ticket = ItemType.get("BASIC_SAFARI_TICKET");
        if (ticket == null) return false;
        data.setFreeTrialDay(today);
        HotfService.save(player, data);
        player.addAndUpdateItem(ticket);
        return true;
    }

    public static boolean freeTrialUsedToday(SkyBlockPlayer player) {
        return HotfService.data(player).getFreeTrialDay() == HotfService.dailyDay();
    }

    public static boolean doubleEssence(SkyBlockPlayer player) {
        double chance = HotfService.perkValue(player, "essence_fortune");
        return chance > 0 && LootRoll.roll(player.getUuid(), null, chance);
    }

    private static long addBoosted(SkyBlockPlayer player, HotfCurrency currency, long base) {
        long boosted = Math.round(base * HotfService.whisperMultiplier(player));
        if (currency == HotfCurrency.FOREST) boosted += ReforgeLoadout.forestWhispers(player, base);
        HotfService.addWhispers(player, currency, boosted);
        return boosted;
    }
}
