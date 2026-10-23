package dev.daweapon.type.skyblockgeneric.rewardbundle;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.generic.user.categories.Rank;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class RewardBundles {
    private static final File FILE = new File("./configuration/skyblock/reward_bundles.yml");
    private static List<BundleReward> rewards;

    private RewardBundles() {
    }

    public static synchronized List<BundleReward> all() {
        if (rewards == null) rewards = load(FILE);
        return rewards;
    }

    public static List<BundleReward> of(BundleSeason season) {
        return all().stream().filter(reward -> reward.season() == season).toList();
    }

    @SuppressWarnings("unchecked")
    public static List<BundleReward> load(File file) {
        try {
            List<BundleReward> loaded = new ArrayList<>();
            for (Map<String, Object> entry : (List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("bundles")) {
                loaded.add(new BundleReward(
                        BundleSeason.valueOf(String.valueOf(entry.get("season"))),
                        ((Number) entry.get("year")).intValue(),
                        Rank.valueOf(String.valueOf(entry.get("rank"))),
                        BundleReward.Type.valueOf(String.valueOf(entry.get("type"))),
                        String.valueOf(entry.get("id")),
                        ((Number) entry.getOrDefault("amount", 1)).intValue()));
            }
            loaded.sort(Comparator.comparing((BundleReward reward) -> reward.rank().ordinal()).reversed()
                    .thenComparingInt(BundleReward::year).thenComparing(BundleReward::type).thenComparing(BundleReward::id));
            return List.copyOf(loaded);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
