package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.BitsMultiplier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class StarlynRewards {
    public static final int DAILY_BONUS_CONTESTS = 3;
    public static final long DAILY_BONUS_AMOUNT = 1_200;
    public static final int DAILY_BONUS_WHISPER_TIER = 8;

    private StarlynRewards() {
    }

    public static double whisperBuff(StarlynSister sister, int milestone) {
        return 1 + StarlynConfig.get(sister).milestone(milestone).whisperBuffPercent() / 100D;
    }

    public static void grant(SkyBlockPlayer player, StarlynSister sister, StarlynBracket bracket) {
        StarlynConfig config = StarlynConfig.get(sister);
        StarlynConfig.BracketReward reward = config.reward(bracket);
        StarlynProfile profile = StarlynContests.profile(player);
        StarlynProfile.SisterData data = profile.sister(sister);

        grantWhispers(player, sister, reward.whispers(), whisperBuff(sister, data.getMilestoneGranted()));
        Essences.add(player, EssenceType.FOREST, reward.essence());
        var skills = player.getSkills();
        skills.setRaw(player, SkillCategories.FORAGING, skills.getRaw(SkillCategories.FORAGING) + reward.foragingXp());
        HotfService.addExperience(player, reward.hotfXp());
        int bits = (int) Math.round(reward.bits() * BitsMultiplier.of(player));
        if (bits > 0) player.addBits(bits);
        player.addAndUpdateItem(sister.getCoupon(), reward.coupons());
        data.setCouponsEarned(data.getCouponsEarned() + reward.coupons());
        if (reward.prizeChance() > 0 && LootRoll.roll(player.getUuid(), sister.getPrize(), reward.prizeChance())) {
            player.addAndUpdateItem(sister.getPrize(), 1);
            player.sendMessage("<9><l>PRIZE! <r><7>You received " + sister.prizeName() + "<7>!");
        }
        grantSafariTicket(player, reward);
        StarlynContests.save(player, profile);
        HotfService.syncSkyBlockXp(player);
        StarlynMilestones.advance(player, sister);
    }

    public static boolean claimDailyBonus(SkyBlockPlayer player, StarlynSister sister) {
        StarlynProfile profile = StarlynContests.profile(player);
        StarlynProfile.SisterData data = profile.sister(sister);
        long today = HotfService.dailyDay();
        if (data.getDailyDay() != today) {
            data.setDailyDay(today);
            data.setDailyClaimed(0);
        }
        if (data.getDailyClaimed() >= DAILY_BONUS_CONTESTS) return false;
        data.setDailyClaimed(data.getDailyClaimed() + 1);
        StarlynContests.save(player, profile);
        if (HotfService.tier(player) >= DAILY_BONUS_WHISPER_TIER) {
            HotfService.addWhispers(player, sister.getCurrency(), DAILY_BONUS_AMOUNT);
            HotfService.syncSkyBlockXp(player);
        } else {
            HotfService.addExperience(player, DAILY_BONUS_AMOUNT);
        }
        return true;
    }

    public static int dailyBonusesLeft(SkyBlockPlayer player, StarlynSister sister) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        return data.getDailyDay() == HotfService.dailyDay() ? Math.max(0, DAILY_BONUS_CONTESTS - data.getDailyClaimed()) : DAILY_BONUS_CONTESTS;
    }

    public static String dailyBonusLine(SkyBlockPlayer player, StarlynSister sister) {
        return HotfService.tier(player) >= DAILY_BONUS_WHISPER_TIER
                ? "<8>  +<a>1,200 " + sister.getCurrency().coloured() + " <e>(Daily Bonus)"
                : "<8>  +<a>1,200 HOTF EXP <e>(Daily Bonus)";
    }

    private static void grantWhispers(SkyBlockPlayer player, StarlynSister sister, long base, double buff) {
        HotfCurrency currency = sister.getCurrency();
        long boosted = Math.round(base * buff * HotfService.whisperMultiplier(player));
        if (currency == HotfCurrency.FOREST) boosted += ReforgeLoadout.forestWhispers(player, base);
        HotfService.addWhispers(player, currency, boosted);
    }

    private static void grantSafariTicket(SkyBlockPlayer player, StarlynConfig.BracketReward reward) {
        ItemType ticket = reward.safariTicket() == null ? null : ItemType.get(reward.safariTicket());
        if (ticket == null) return;
        int tickets = 1;
        double extra = StarlynContests.extraTicketChance(player);
        if (extra > 0 && LootRoll.roll(player.getUuid(), null, extra)) tickets++;
        player.addAndUpdateItem(ticket, tickets);
    }
}
