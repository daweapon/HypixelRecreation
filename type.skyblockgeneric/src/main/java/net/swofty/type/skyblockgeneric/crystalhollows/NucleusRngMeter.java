package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class NucleusRngMeter {
    public static final long XP_PER_RUN = 1_000;
    private static final double MAX_BOOST = 2;
    private static final double GUARANTEED_WEIGHT = 1e12;

    private NucleusRngMeter() {
    }

    public static BundleReward selected(SkyBlockPlayer player) {
        String id = CrystalHollowsAccess.data(player).getRngSelected();
        BundleReward reward = id == null ? null : BundleTable.byId(id);
        return reward != null && reward.hasMeter() ? reward : null;
    }

    public static long stored(SkyBlockPlayer player) {
        return CrystalHollowsAccess.data(player).getRngXp();
    }

    public static void select(SkyBlockPlayer player, BundleReward reward) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        data.setRngSelected(reward == null ? null : reward.id());
        CrystalHollowsAccess.save(player, data);
    }

    public static void addRun(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        data.setRngXp(data.getRngXp() + XP_PER_RUN);
        CrystalHollowsAccess.save(player, data);
    }

    public static void reset(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        data.setRngXp(0);
        CrystalHollowsAccess.save(player, data);
    }

    public static boolean full(SkyBlockPlayer player, BundleReward reward) {
        return reward != null && stored(player) >= reward.meterXp();
    }

    public static double percent(SkyBlockPlayer player, BundleReward reward) {
        return Math.min(100D, stored(player) * 100D / reward.meterXp());
    }

    public static double weight(SkyBlockPlayer player, BundleReward reward) {
        BundleReward selected = selected(player);
        if (selected == null || !selected.id().equals(reward.id())) return reward.weight();
        if (full(player, reward)) return GUARANTEED_WEIGHT;
        double boost = Math.min(MAX_BOOST * stored(player) / reward.meterXp(), MAX_BOOST);
        return reward.weight() * (1 + boost);
    }

    public static double chancePercent(SkyBlockPlayer player, BundleReward reward) {
        double total = BundleTable.rewards().stream().mapToDouble(entry -> weight(player, entry)).sum();
        return weight(player, reward) * 100D / total;
    }
}
