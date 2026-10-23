package dev.daweapon.type.skyblockgeneric.rewardbundle;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.spray.SprayCatalogue;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.FurnitureCatalog;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardBundlesTest {
    private static final List<BundleReward> REWARDS = RewardBundles.load(new File("../configuration/skyblock/reward_bundles.yml"));

    @Test
    void holdsEveryRewardTheWikiListsForTheFourSeasons() {
        assertEquals(263, REWARDS.size());
        assertEquals(127, REWARDS.stream().filter(reward -> reward.type() == BundleReward.Type.FURNITURE).count());
        assertEquals(87, REWARDS.stream().filter(reward -> reward.type() == BundleReward.Type.SPRAY).count());
        assertEquals(49, REWARDS.stream().filter(reward -> reward.type() == BundleReward.Type.MINION_SKIN).count());
        for (BundleSeason season : BundleSeason.values()) assertTrue(RewardBundles.of(season).size() > 40, season.name());
    }

    @Test
    void everyRewardKeyIsUnique() {
        Set<String> keys = new HashSet<>();
        REWARDS.forEach(reward -> assertTrue(keys.add(reward.key()), reward.key()));
    }

    @Test
    void everyFurnitureRewardCanBePlacedAndEverySprayExists() {
        REWARDS.forEach(reward -> {
            switch (reward.type()) {
                case FURNITURE -> assertTrue(FurnitureCatalog.get(reward.id()) != null, reward.id());
                case SPRAY -> assertTrue(SprayCatalogue.byId(reward.id()).isPresent(), reward.id());
                case MINION_SKIN -> assertTrue(itemIds().contains(reward.id()), reward.id());
            }
        });
    }

    @Test
    void ranksStartAtVipAndNeverRequireStaff() {
        REWARDS.forEach(reward -> assertTrue(reward.rank().ordinal() >= Rank.MVP_PLUS_PLUS.ordinal() && reward.rank().ordinal() <= Rank.VIP.ordinal(), reward.key()));
    }

    @Test
    void seasonsFollowTheCalendarAndCycleBothWays() {
        assertEquals(BundleSeason.EASTER, BundleSeason.current(LocalDate.of(2026, 4, 3)));
        assertEquals(BundleSeason.SUMMER, BundleSeason.current(LocalDate.of(2026, 8, 31)));
        assertNull(BundleSeason.current(LocalDate.of(2026, 9, 1)));
        assertEquals(BundleSeason.HALLOWEEN, BundleSeason.current(LocalDate.of(2026, 10, 22)));
        assertEquals(BundleSeason.HOLIDAYS, BundleSeason.current(LocalDate.of(2026, 12, 24)));
        assertEquals(BundleSeason.EASTER, BundleSeason.HOLIDAYS.next());
        assertEquals(BundleSeason.HOLIDAYS, BundleSeason.EASTER.previous());
    }

    @SuppressWarnings("unchecked")
    private static Set<String> itemIds() {
        try (Stream<Path> files = Files.walk(Path.of("../configuration/skyblock/items"))) {
            return files.filter(path -> path.toString().endsWith(".yml")).flatMap(path -> {
                try {
                    Object items = YamlFileUtils.loadYaml(path.toFile()).get("items");
                    return items instanceof List<?> list ? ((List<Map<String, Object>>) list).stream() : Stream.<Map<String, Object>>empty();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            }).map(item -> String.valueOf(item.get("id"))).collect(Collectors.toSet());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
