package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Buying rules shared by every essence perk shop: one essence type, a fixed perk list, and a
 * SkyBlock XP source paid per tier owned.
 */
public final class EssenceShop<P extends EssencePerk> {
    public enum Result {BOUGHT, UNAVAILABLE, TOO_POOR, MAXED}

    private final EssenceType essence;
    private final List<P> perks;
    private final Map<P, int[]> tierXP;
    private final SkyBlockXPSource xpSource;

    public EssenceShop(EssenceType essence, List<P> perks, Map<P, int[]> tierXP, SkyBlockXPSource xpSource) {
        this.essence = essence;
        this.perks = perks;
        this.tierXP = tierXP;
        this.xpSource = xpSource;
    }

    public EssenceType essence() {
        return essence;
    }

    public List<P> perks() {
        return perks;
    }

    public Result buy(SkyBlockPlayer player, P perk) {
        int owned = EssencePerks.tier(player, perk);
        Result check = check(owned, perk, Essences.get(player, essence));
        if (check != Result.BOUGHT) return check;
        if (!Essences.take(player, essence, perk.costOfTier(owned + 1))) return Result.TOO_POOR;
        EssencePerks.setTier(player, perk, owned + 1);
        syncSkyBlockXP(player);
        return Result.BOUGHT;
    }

    public Result check(int owned, P perk, long balance) {
        if (owned >= perk.getMaxTier()) return Result.MAXED;
        if (perk.getUnavailableReason() != null) return Result.UNAVAILABLE;
        if (balance < perk.costOfTier(owned + 1)) return Result.TOO_POOR;
        return Result.BOUGHT;
    }

    public void maxAvailable(SkyBlockPlayer player) {
        for (P perk : perks) {
            if (perk.getUnavailableReason() == null) EssencePerks.setTier(player, perk, perk.getMaxTier());
        }
        syncSkyBlockXP(player);
    }

    public void syncSkyBlockXP(SkyBlockPlayer player) {
        if (xpSource == null) return;
        player.getSkyBlockExperience().setSourceXP(xpSource, skyBlockXP(perk -> EssencePerks.tier(player, perk)));
    }

    public int skyBlockXP(ToIntFunction<P> tiers) {
        int total = 0;
        for (P perk : perks) {
            int[] perTier = tierXP.get(perk);
            for (int tier = 0; tier < Math.min(tiers.applyAsInt(perk), perTier.length); tier++) total += perTier[tier];
        }
        return total;
    }

    public int maxSkyBlockXP() {
        return skyBlockXP(EssencePerk::getMaxTier);
    }
}
