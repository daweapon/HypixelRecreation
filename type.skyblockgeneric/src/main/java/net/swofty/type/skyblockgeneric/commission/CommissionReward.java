package net.swofty.type.skyblockgeneric.commission;

import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// The Heart of the Mountain XP, mining XP, and powder granted for completing a commission.
public record CommissionReward(int hotmXp, int miningXp, int mithrilPowder, int glacitePowder, int gemstonePowder, boolean isDailyBonus) {
	public static CommissionReward calculateHollows(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		int hotmXp = isHotmMaxed ? 0 : 400 + (isDailyBonus ? 900 : 0);
		int gemstonePowder = (hotmTier >= 10 ? 2_000 : 1_000) + (isDailyBonus && isHotmMaxed ? 3_200 : 0);
		return new CommissionReward(hotmXp, ThreadLocalRandom.current().nextInt(10_000, 20_001), 0, 0, gemstonePowder, isDailyBonus);
	}

	public static List<Text> getHollowsRewardLore(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		List<Text> lore = new ArrayList<>();
		if (!isHotmMaxed) {
			lore.add(Text.of(isDailyBonus ? "<7>- <5>+{} Heart of the Mountain XP <e>(Daily Bonus)" : "<7>- <5>+{} Heart of the Mountain XP", isDailyBonus ? 1300 : 400));
		}
		lore.add(Text.of("<7>- <d>+{} Gemstone Powder", String.format("%,d", (hotmTier >= 10 ? 2_000 : 1_000) + (isDailyBonus && isHotmMaxed ? 3_200 : 0))));
		lore.add(Text.of("<7>- <3>+15k Mining XP"));
		return lore;
	}


	public static CommissionReward calculateGlacite(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		int hotmXp = 750 + (isDailyBonus && !isHotmMaxed ? 900 : 0);
		int glacitePowder = (hotmTier >= 10 ? 3_000 : 1_500) + (isDailyBonus && isHotmMaxed ? 4_000 : 0);
		return new CommissionReward(isHotmMaxed ? 0 : hotmXp, ThreadLocalRandom.current().nextInt(12_500, 25_001), 0, glacitePowder, 0, isDailyBonus);
	}

	public static List<Text> getGlaciteRewardLore(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		List<Text> lore = new ArrayList<>();
		if (!isHotmMaxed) {
			lore.add(Text.of(isDailyBonus ? "<7>- <5>+{} Heart of the Mountain XP <e>(Daily Bonus)" : "<7>- <5>+{} Heart of the Mountain XP", isDailyBonus ? 1650 : 750));
		}
		lore.add(Text.of("<7>- <b>+{} Glacite Powder", String.format("%,d", (hotmTier >= 10 ? 3_000 : 1_500) + (isDailyBonus && isHotmMaxed ? 4_000 : 0))));
		lore.add(Text.of("<7>- <3>+18.8k Mining XP"));
		return lore;
	}

	public static CommissionReward calculate(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		int hotmXp = isHotmMaxed ? 0 : baseHotmXp(hotmTier) + (isDailyBonus ? 900 : 0);
		int mithrilPowder = mithrilPowder(hotmTier) + (isDailyBonus && isHotmMaxed ? 1_600 : 0);
		return new CommissionReward(hotmXp, ThreadLocalRandom.current().nextInt(5_000, 10_001), mithrilPowder, 0, 0, isDailyBonus);
	}

	public static List<Text> getRewardLore(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		List<Text> lore = new ArrayList<>();
		if (!isHotmMaxed) {
			int hotmXp = baseHotmXp(hotmTier) + (isDailyBonus ? 900 : 0);
			lore.add(Text.of(isDailyBonus ? "<7>- <5>+{} Heart of the Mountain XP <e>(Daily Bonus)" : "<7>- <5>+{} Heart of the Mountain XP", hotmXp));
		}
		lore.add(Text.of("<7>- <2>+{} Mithril Powder", String.format("%,d", mithrilPowder(hotmTier) + (isDailyBonus && isHotmMaxed ? 1_600 : 0))));
		lore.add(Text.of("<7>- <3>+7.5k Mining XP"));
		return lore;
	}

	private static int baseHotmXp(int hotmTier) {
		return hotmTier <= 1 ? 100 : hotmTier == 2 ? 200 : 400;
	}

	private static int mithrilPowder(int hotmTier) {
		return hotmTier >= 10 ? 1_000 : 500;
	}
}
