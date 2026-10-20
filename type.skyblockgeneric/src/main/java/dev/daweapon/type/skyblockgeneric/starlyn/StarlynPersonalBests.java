package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class StarlynPersonalBests {
    public static final long MAX_COLLECTION = 100_000;
    public static final double SWEEP_PERCENT_PER_HUNDRED = 0.01;

    private StarlynPersonalBests() {
    }

    public static double sweepPercent(long personalBest) {
        return Math.floor(Math.min(personalBest, MAX_COLLECTION) / 100D) * SWEEP_PERCENT_PER_HUNDRED;
    }

    static void update(SkyBlockPlayer player, StarlynSister sister, StarlynProfile.SisterData data, StarlynProfile.Entry entry) {
        for (StarlynConfig.ShopEntry shopEntry : StarlynConfig.get(sister).shop()) {
            if (!(shopEntry instanceof StarlynConfig.PersonalBest perk) || data.purchased(perk.id()) <= 0) continue;
            long collected = entry.logs(perk.tree());
            long previous = data.personalBest(perk.tree());
            if (collected <= previous) continue;
            data.getPersonalBests().put(perk.tree(), collected);
            player.sendMessage(StarlynDialogue.speaker(sister) + "<d><l>PERSONAL BEST<r><f>! You collected <e>"
                    + StringUtility.commaify(collected) + " <f>" + perk.tree().getDisplayName() + " Logs, beating your record of <e>"
                    + StringUtility.commaify(previous) + "<f>!");
        }
    }
}
