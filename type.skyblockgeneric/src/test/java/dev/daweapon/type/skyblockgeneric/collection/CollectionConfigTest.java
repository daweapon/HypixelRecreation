package dev.daweapon.type.skyblockgeneric.collection;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionConfigTest {
    private static final Map<String, Integer> EXPECTED_COUNTS = Map.of(
            "farming.yml", 20, "mining.yml", 25, "combat.yml", 11, "foraging.yml", 14, "fishing.yml", 12);

    @Test
    void everyCategoryHasTheWikiCollectionCount() throws Exception {
        for (Map.Entry<String, Integer> expected : EXPECTED_COUNTS.entrySet()) {
            assertEquals(expected.getValue(), collections(expected.getKey()).size(), expected.getKey());
        }
    }

    @Test
    void everyCollectionItemIsUniqueAndExists() throws Exception {
        Set<String> seen = new HashSet<>();
        List<String> problems = new ArrayList<>();
        for (String file : EXPECTED_COUNTS.keySet()) {
            for (Map<String, Object> collection : collections(file)) {
                String id = (String) collection.get("itemType");
                if (ItemType.get(id) == null) problems.add("missing item " + id);
                if (!seen.add(id)) problems.add("duplicate " + id);
            }
        }
        assertTrue(problems.isEmpty(), problems.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyRewardReferencesAKnownItemOrStatistic() throws Exception {
        List<String> problems = new ArrayList<>();
        for (String file : EXPECTED_COUNTS.keySet()) {
            for (Map<String, Object> collection : collections(file)) {
                int previous = 0;
                for (Map<String, Object> tier : (List<Map<String, Object>>) collection.get("rewards")) {
                    int amount = (Integer) tier.get("amount");
                    if (amount <= previous) problems.add(collection.get("itemType") + " tiers not ascending at " + amount);
                    previous = amount;
                    for (Map<String, Object> reward : (List<Map<String, Object>>) tier.get("rewards")) {
                        Map<String, Object> data = (Map<String, Object>) reward.get("data");
                        String type = (String) reward.get("type");
                        if (type.equals("RECIPE_UNLOCK") && ItemType.get((String) data.get("unlockedItemType")) == null) {
                            problems.add(collection.get("itemType") + " unlocks unknown " + data.get("unlockedItemType"));
                        }
                        if (type.equals("STAT")) {
                            try {
                                ItemStatistic.valueOf((String) data.get("stat"));
                            } catch (IllegalArgumentException e) {
                                problems.add(collection.get("itemType") + " unknown stat " + data.get("stat"));
                            }
                        }
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> collections(String file) throws Exception {
        Map<String, Object> data = YamlFileUtils.loadYaml(new File("../configuration/skyblock/collections", file));
        return (List<Map<String, Object>>) data.get("collections");
    }
}
