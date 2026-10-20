package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.Map;

public final class HotfStatistics {
    public static final int EARLY_BIRD_TREES = 250;
    private static final int OPPOSITE_PLAYER_CAP = 5;
    private static final double SPEED_CAP = 500;
    private static final double STRENGTH_CAP = 1000;
    private static final List<ItemStatistic> COMBAT_STATISTICS = List.of(ItemStatistic.STRENGTH, ItemStatistic.CRITICAL_CHANCE,
            ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.DEFENSE, ItemStatistic.HEALTH, ItemStatistic.INTELLIGENCE);

    private HotfStatistics() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        if (!data.isUnlocked()) return ItemStatistics.empty();
        boolean onIsland = HotfService.onForagingIsland();
        ItemStatistics total = ItemStatistics.empty();
        for (Map.Entry<String, Integer> entry : data.getPerkLevels().entrySet()) {
            HotfPerk perk = HotfRegistry.get().perk(entry.getKey());
            if (perk == null || !perk.available() || data.getDisabledPerks().contains(entry.getKey())) continue;
            if (perk.id().equals("early_bird") && !earlyBirdActive(data)) continue;
            for (HotfPerk.Effect effect : perk.effects()) {
                if (effect.statistic() == null) continue;
                if (effect.scope() == HotfScope.FORAGING_ISLAND && !onIsland) continue;
                total = total.addBase(effect.statistic(), effect.value(entry.getValue()));
            }
        }
        total = ItemStatistics.add(total, lottery(player));
        total = ItemStatistics.add(total, galateasMight(player, onIsland));
        total = ItemStatistics.add(total, halves(player));
        return ItemStatistics.add(total, HotfAbilities.statistics(player));
    }

    public static ItemStatistics derived(SkyBlockPlayer player, ItemStatistics total) {
        double speedPercent = HotfService.perkValue(player, "forest_speed");
        double strengthPercent = HotfService.perkValue(player, "forest_strength");
        if (speedPercent <= 0 && strengthPercent <= 0) return total;
        double speed = Math.min(SPEED_CAP, total.getOverall(ItemStatistic.SPEED));
        double strength = Math.min(STRENGTH_CAP, total.getOverall(ItemStatistic.STRENGTH));
        double bonus = speed * speedPercent / 100 + strength * strengthPercent / 100;
        return total.addBase(ItemStatistic.FORAGING_FORTUNE, bonus).addBase(ItemStatistic.SWEEP, bonus);
    }

    public static boolean earlyBirdActive(DatapointHOTF.PlayerHOTFData data) {
        return data.getTreesDay() != HotfService.dailyDay() || data.getTreesToday() < EARLY_BIRD_TREES;
    }

    private static ItemStatistics lottery(SkyBlockPlayer player) {
        HotfLottery effect = HotfLottery.current(player);
        if (effect == null) return ItemStatistics.empty();
        return switch (effect) {
            case FIG_FORTUNE -> ItemStatistics.empty().addBase(ItemStatistic.FIG_FORTUNE, 50D);
            case MANGROVE_FORTUNE -> ItemStatistics.empty().addBase(ItemStatistic.MANGROVE_FORTUNE, 50D);
            case SWEEP -> ItemStatistics.builder().withMultiplicativePercentage(ItemStatistic.SWEEP, 5D).build();
        };
    }

    private static ItemStatistics galateasMight(SkyBlockPlayer player, boolean onIsland) {
        double percent = HotfService.perkValue(player, "galateas_might");
        if (!onIsland || percent <= 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : COMBAT_STATISTICS) builder.withMultiplicativePercentage(statistic, percent);
        return builder.build();
    }

    private static ItemStatistics halves(SkyBlockPlayer player) {
        ItemStatistics total = ItemStatistics.empty();
        for (String own : List.of("half_full", "half_empty")) {
            if (!HotfService.hasPerk(player, own)) continue;
            HotfPerk perk = HotfRegistry.get().perk(own);
            int count = playersWith(player, perk.exclusive());
            int level = HotfService.perkLevel(player, own);
            total = total.addBase(ItemStatistic.FORAGING_FORTUNE, perk.value("main", level) * count)
                    .addBase(ItemStatistic.SWEEP, perk.value("second", level) * count);
        }
        return total;
    }

    private static int playersWith(SkyBlockPlayer player, String perkId) {
        Instance instance = player.getInstance();
        if (instance == null) return 0;
        int count = 0;
        for (Player other : instance.getPlayers()) {
            if (other != player && other instanceof SkyBlockPlayer skyBlock && HotfService.hasPerk(skyBlock, perkId)) count++;
        }
        return Math.min(count, OPPOSITE_PLAYER_CAP);
    }
}
