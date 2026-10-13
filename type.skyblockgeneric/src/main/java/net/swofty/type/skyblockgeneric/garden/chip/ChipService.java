package net.swofty.type.skyblockgeneric.garden.chip;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ChipService {
    private static final long[] LEVEL_COST = {100_000, 200_000, 300_000, 400_000, 550_000, 700_000, 850_000, 1_000_000, 1_150_000,
            1_300_000, 1_450_000, 1_600_000, 1_750_000, 1_900_000, 2_050_000, 2_200_000, 2_350_000, 2_500_000, 2_650_000};

    public enum UpgradeResult {
        UPGRADED, LOCKED, MAX_FOR_RARITY, NOT_ENOUGH_SOWDUST
    }

    private ChipService() {
    }

    public static ChipData data(SkyBlockPlayer player) {
        return Gardens.profile(player).chips();
    }

    public static long costToReach(int level) {
        return level < 2 || level - 2 >= LEVEL_COST.length ? 0 : LEVEL_COST[level - 2];
    }

    public static int maxLevel(ChipData data, GardenChip chip) {
        int rarity = data.rarity(chip);
        return rarity < 0 ? 0 : GardenChip.MAX_LEVEL[rarity];
    }

    public static void consume(SkyBlockPlayer player, GardenChip chip, int count) {
        GardenProfile profile = Gardens.profile(player);
        ChipData data = profile.chips();
        int before = data.rarity(chip);
        data.setConsumed(chip, data.consumed(chip) + count);
        if (data.level(chip) == 0) data.setLevel(chip, 1);
        int after = data.rarity(chip);
        Gardens.save(player, profile);
        if (before < 0) {
            player.sendMessage(Text.of("<a>You unlocked the {} Chip<a>! <8>({})", chip.getDisplayName(), GardenChip.RARITY_NAMES[after]));
        } else if (after > before) {
            player.sendMessage(Text.of("<a>Your {} Chip <a>is now {}{}<a>!", chip.getDisplayName(), GardenChip.RARITY_COLOURS[after], GardenChip.RARITY_NAMES[after]));
        } else {
            player.sendMessage(Text.of("<a>You redeemed a {} Chip<a>.", chip.getDisplayName()));
        }
    }

    public static UpgradeResult upgrade(SkyBlockPlayer player, GardenChip chip) {
        GardenProfile profile = Gardens.profile(player);
        ChipData data = profile.chips();
        if (!data.unlocked(chip)) return UpgradeResult.LOCKED;
        int level = data.level(chip);
        if (level >= maxLevel(data, chip)) return UpgradeResult.MAX_FOR_RARITY;
        long cost = costToReach(level + 1);
        if (!profile.spendSowdust(cost)) return UpgradeResult.NOT_ENOUGH_SOWDUST;
        data.setLevel(chip, level + 1);
        Gardens.save(player, profile);
        return UpgradeResult.UPGRADED;
    }

    public static double bonus(ChipData data, GardenChip chip) {
        int rarity = data.rarity(chip);
        if (rarity < 0 || !chip.isActive()) return 0;
        return chip.getPerLevel()[rarity] * data.level(chip);
    }

    public static double bonus(SkyBlockPlayer player, GardenChip chip) {
        return bonus(data(player), chip);
    }

    public static String describe(ChipData data, GardenChip chip) {
        int rarity = Math.max(0, data.rarity(chip));
        double value = chip.getPerLevel()[rarity] * Math.max(1, data.level(chip));
        return "+" + StringUtility.commaify(value) + chip.getUnit() + chip.getSuffix();
    }
}
