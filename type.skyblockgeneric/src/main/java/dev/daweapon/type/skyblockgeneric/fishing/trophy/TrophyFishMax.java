package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * /maxprofile for trophy fishing: every tier of every Trophy Fish caught (so Master Trophy Fisher, which unlocks
 * Thunder and Lord Jawbus), Odger and Hattie met, and the Trophy Fish SkyBlock XP. Odger's rank rewards stay
 * unclaimed, since they are items.
 */
public final class TrophyFishMax {
    private TrophyFishMax() {
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            DatapointTrophyFish.FishProgress progress = data.getProgress(fish.id());
            progress.setBronze(Math.max(1, progress.getBronze()));
            progress.setSilver(Math.max(1, progress.getSilver()));
            progress.setGold(Math.max(1, progress.getGold()));
            progress.setDiamond(Math.max(1, progress.getDiamond()));
            progress.setTotalCatches(Math.max(progress.getTotalCatches(),
                    progress.getBronze() + progress.getSilver() + progress.getGold() + progress.getDiamond()));
        }
        data.setOdgerMet(true);
        TrophyFishAwards.save(player, data);
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_HATTIE, true);
        TrophyFishAwards.syncSkyBlockXP(player);
    }
}
