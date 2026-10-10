package net.swofty.type.skyblockgeneric.gifts;

import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

final class GiftAchievements {
    private static final int STUBBORN_GIFTER_TOTAL = 1000;

    private GiftAchievements() {
    }

    static void record(SkyBlockPlayer giver, SkyBlockPlayer recipient) {
        DatapointStringList recipients = giver.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.GIFT_RECIPIENTS, DatapointStringList.class);
        List<String> known = new ArrayList<>(recipients.getValue());
        String id = recipient.getUuid().toString();
        if (!known.contains(id)) {
            known.add(id);
            recipients.setValue(known);
        }
        giver.getAchievementHandler().setProgress("skyblock.festive_altruist", known.size());
        if (SnowSuit.giftsGiven(giver) >= STUBBORN_GIFTER_TOTAL) {
            giver.getAchievementHandler().completeAchievement("skyblock.stubborn_gifter");
        }
    }
}
