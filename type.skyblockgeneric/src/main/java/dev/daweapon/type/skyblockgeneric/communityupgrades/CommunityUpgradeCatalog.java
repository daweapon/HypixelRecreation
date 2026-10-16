package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.commons.YamlFileUtils;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CommunityUpgradeCatalog {
    public record Tier(double value, int gems, long hours, double extra) {
    }

    private static final File FILE = new File("./configuration/skyblock/community_upgrades.yml");
    private static volatile Map<CommunityUpgradeId, List<Tier>> loaded;

    private CommunityUpgradeCatalog() {
    }

    public static List<Tier> tiers(CommunityUpgradeId id) {
        return all().get(id);
    }

    public static int maxTier(CommunityUpgradeId id) {
        return tiers(id).size();
    }

    public static Map<CommunityUpgradeId, List<Tier>> all() {
        Map<CommunityUpgradeId, List<Tier>> result = loaded;
        if (result == null) {
            result = load(FILE);
            loaded = result;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<CommunityUpgradeId, List<Tier>> load(File file) {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(file);
            Map<String, Map<String, Object>> upgrades = (Map<String, Map<String, Object>>) root.get("upgrades");
            Map<CommunityUpgradeId, List<Tier>> result = new EnumMap<>(CommunityUpgradeId.class);
            for (CommunityUpgradeId id : CommunityUpgradeId.values()) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) upgrades.get(id.name()).get("tiers");
                result.put(id, rows.stream().map(row -> new Tier(
                        ((Number) row.get("value")).doubleValue(),
                        ((Number) row.get("gems")).intValue(),
                        ((Number) row.get("hours")).longValue(),
                        row.get("extra") == null ? 0 : ((Number) row.get("extra")).doubleValue())).toList());
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load community upgrades from " + file, exception);
        }
    }
}
