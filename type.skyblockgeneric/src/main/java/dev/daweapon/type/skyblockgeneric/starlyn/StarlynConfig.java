package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StarlynConfig {
    public static final Path DIRECTORY = Path.of("./configuration/skyblock/starlyn");
    private static final Map<StarlynSister, StarlynConfig> LOADED = new ConcurrentHashMap<>();

    private final StarlynSister sister;
    private final String serverType;
    private final Points points;
    private final List<BracketReward> brackets;
    private final List<Milestone> milestones;
    private final List<ShopEntry> shop;

    private StarlynConfig(StarlynSister sister, String serverType, Points points, List<BracketReward> brackets,
                          List<Milestone> milestones, List<ShopEntry> shop) {
        this.sister = sister;
        this.serverType = serverType;
        this.points = points;
        this.brackets = brackets;
        this.milestones = milestones;
        this.shop = shop;
    }

    public static StarlynConfig get(StarlynSister sister) {
        return LOADED.computeIfAbsent(sister, key -> load(key, DIRECTORY));
    }

    public static StarlynConfig load(StarlynSister sister, Path directory) {
        Map<String, Object> root = read(directory.resolve(sister.key() + ".yml"));
        List<BracketReward> brackets = new ArrayList<>();
        for (Map<String, Object> entry : maps(root.get("brackets"))) {
            brackets.add(new BracketReward(StarlynBracket.valueOf((String) entry.get("bracket")),
                    integer(entry, "points"), integer(entry, "coupons"), longValue(entry, "whispers"),
                    integer(entry, "essence"), longValue(entry, "foragingXp"), longValue(entry, "hotfXp"),
                    integer(entry, "bits"), decimal(entry, "prizeChance"), (String) entry.get("safariTicket")));
        }
        List<Milestone> milestones = new ArrayList<>();
        for (Map<String, Object> entry : maps(root.get("milestones"))) {
            milestones.add(new Milestone(integer(entry, "number"), integer(entry, "threshold"),
                    longValue(entry, "foragingXp"), integer(entry, "essence"), integer(entry, "whisperBuffPercent")));
        }
        List<ShopEntry> shop = new ArrayList<>();
        for (Map<String, Object> entry : maps(root.get("shop"))) shop.add(parseShopEntry(entry));
        return new StarlynConfig(sister, (String) root.get("serverType"), parsePoints(map(root.get("points"))),
                List.copyOf(brackets), List.copyOf(milestones), List.copyOf(shop));
    }

    public static void reload() {
        LOADED.clear();
    }

    public StarlynSister sister() {
        return sister;
    }

    public String serverType() {
        return serverType;
    }

    public Points points() {
        return points;
    }

    public List<BracketReward> brackets() {
        return brackets;
    }

    public List<Milestone> milestones() {
        return milestones;
    }

    public List<ShopEntry> shop() {
        return shop;
    }

    public int maxMilestone() {
        return milestones.getLast().number();
    }

    public @Nullable BracketReward bracketFor(double score) {
        BracketReward reached = null;
        for (BracketReward reward : brackets) if (score >= reward.points()) reached = reward;
        return reached;
    }

    public BracketReward reward(StarlynBracket bracket) {
        return brackets.stream().filter(reward -> reward.bracket() == bracket).findFirst().orElseThrow();
    }

    public Milestone milestone(int number) {
        return milestones.get(number - 1);
    }

    public int milestoneFor(long couponsEarned) {
        int reached = 1;
        for (Milestone milestone : milestones) if (couponsEarned >= milestone.threshold()) reached = milestone.number();
        return reached;
    }

    public @Nullable ShopEntry entry(String id) {
        return shop.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
    }

    public record BracketReward(StarlynBracket bracket, int points, int coupons, long whispers, int essence,
                                long foragingXp, long hotfXp, int bits, double prizeChance, @Nullable String safariTicket) {
    }

    public record Milestone(int number, int threshold, long foragingXp, int essence, int whisperBuffPercent) {
    }

    public record Points(int treeGift, Map<StarlynTree, Integer> logsPerHundred, int huntingDefault,
                         Map<String, Integer> huntingByShard, Map<String, Integer> huntingByRarity, int seaCreature,
                         int treasure, Map<String, Integer> treasureByQuality, int catchPoints, int combat,
                         Map<StarlynResource, Integer> gathering) {
    }

    public sealed interface ShopEntry permits LevelCap, Power, Fortune, PersonalBest, Item {
        String id();
    }

    public record CapTier(CustomCollectionAward award, int milestone, StarlynCost cost) {
    }

    public record LevelCap(String id, Material icon, List<CapTier> tiers) implements ShopEntry {
    }

    public record PowerTier(int milestone, int coupons, double sweep) {
    }

    public record Power(String id, String name, Material icon, List<PowerTier> tiers) implements ShopEntry {
    }

    public record Fortune(String id, String name, StarlynTree tree, @Nullable ItemStatistic statistic, ItemType icon,
                          int milestone, int perLevel, int maxLevel, int couponStep, int couponCap, ItemType logItem,
                          List<Integer> logs, ItemType materialItem, List<Integer> material, int materialAfter,
                          int prizeFromLevel) implements ShopEntry {
        public StarlynCost cost(int level) {
            Map<ItemType, Integer> items = new LinkedHashMap<>();
            if (level <= logs.size()) items.put(logItem, logs.get(level - 1));
            int materialIndex = level - logs.size() - 1;
            if (materialIndex >= 0) items.put(materialItem, materialIndex < material.size() ? material.get(materialIndex) : materialAfter);
            return new StarlynCost(Math.min(couponCap, couponStep * level), level >= prizeFromLevel ? 1 : 0, items);
        }

        public StarlynCost totalCost(int level) {
            StarlynCost total = StarlynCost.FREE;
            for (int current = 1; current <= level; current++) total = total.plus(cost(current));
            return total;
        }

        public int fortune(int level) {
            return level * perLevel;
        }
    }

    public record PersonalBest(String id, String name, StarlynTree tree, Material icon, int milestone, int coupons)
            implements ShopEntry {
    }

    public record Give(String type, @Nullable ItemType item, @Nullable String shard, @Nullable String enchant, int level,
                       @Nullable Rarity rarity) {
    }

    public record Item(String id, int milestone, StarlynCost cost, Give give) implements ShopEntry {
    }

    private static Points parsePoints(Map<String, Object> root) {
        Map<StarlynTree, Integer> logs = new EnumMap<>(StarlynTree.class);
        map(root.get("logsPerHundred")).forEach((key, value) -> logs.put(StarlynTree.valueOf(key), ((Number) value).intValue()));
        Map<String, Object> hunting = map(root.get("hunting"));
        Map<String, Integer> byShard = new LinkedHashMap<>();
        map(hunting.get("byShard")).forEach((key, value) -> byShard.put(key.toLowerCase(Locale.ROOT), ((Number) value).intValue()));
        Map<String, Integer> byRarity = new LinkedHashMap<>();
        map(hunting.get("byRarity")).forEach((key, value) -> byRarity.put(key, ((Number) value).intValue()));
        Map<String, Object> fishing = map(root.get("fishing"));
        Map<String, Integer> quality = new LinkedHashMap<>();
        map(fishing.get("treasureByQuality")).forEach((key, value) -> quality.put(key, ((Number) value).intValue()));
        Map<StarlynResource, Integer> gathering = new EnumMap<>(StarlynResource.class);
        map(root.get("gathering")).forEach((key, value) -> gathering.put(StarlynResource.valueOf(key), ((Number) value).intValue()));
        return new Points(integer(root, "treeGift"), Map.copyOf(logs), integer(hunting, "default"), Map.copyOf(byShard),
                Map.copyOf(byRarity), integer(fishing, "seaCreature"), integer(fishing, "treasure"), Map.copyOf(quality),
                integer(fishing, "catch"), integer(root, "combat"), Map.copyOf(gathering));
    }

    private static ShopEntry parseShopEntry(Map<String, Object> entry) {
        String id = (String) entry.get("id");
        String name = (String) entry.get("name");
        return switch ((String) entry.get("kind")) {
            case "LEVEL_CAP" -> {
                List<CapTier> tiers = new ArrayList<>();
                for (Map<String, Object> tier : maps(entry.get("tiers"))) {
                    tiers.add(new CapTier(CustomCollectionAward.valueOf((String) tier.get("award")), integer(tier, "milestone"),
                            new StarlynCost(integer(tier, "coupons"), integer(tier, "prizes"), Map.of())));
                }
                yield new LevelCap(id, material(entry.get("icon")), List.copyOf(tiers));
            }
            case "POWER" -> {
                List<PowerTier> tiers = new ArrayList<>();
                for (Map<String, Object> tier : maps(entry.get("tiers"))) {
                    tiers.add(new PowerTier(integer(tier, "milestone"), integer(tier, "coupons"), decimal(tier, "sweep")));
                }
                yield new Power(id, name, material(entry.get("icon")), List.copyOf(tiers));
            }
            case "FORTUNE" -> {
                String statistic = (String) entry.get("statistic");
                yield new Fortune(id, name, StarlynTree.valueOf((String) entry.get("tree")),
                        statistic == null ? null : ItemStatistic.valueOf(statistic), item(entry.get("iconItem")),
                        integer(entry, "milestone"), integer(entry, "perLevel"), integer(entry, "maxLevel"),
                        integer(entry, "couponStep"), integer(entry, "couponCap"), item(entry.get("logItem")),
                        integers(entry.get("logs")), item(entry.get("materialItem")), integers(entry.get("material")),
                        integer(entry, "materialAfter"), integer(entry, "prizeFromLevel"));
            }
            case "PERSONAL_BEST" -> new PersonalBest(id, name, StarlynTree.valueOf((String) entry.get("tree")),
                    material(entry.get("icon")), integer(entry, "milestone"), integer(entry, "coupons"));
            case "ITEM" -> {
                Map<ItemType, Integer> requires = new LinkedHashMap<>();
                map(entry.get("requires")).forEach((key, value) -> requires.put(item(key), ((Number) value).intValue()));
                Map<String, Object> give = map(entry.get("give"));
                String rarity = (String) give.get("rarity");
                yield new Item(id, integer(entry, "milestone"),
                        new StarlynCost(integer(entry, "coupons"), integer(entry, "prizes"), requires),
                        new Give((String) give.get("type"), give.get("item") == null ? null : item(give.get("item")),
                                (String) give.get("shard"), (String) give.get("enchant"), integer(give, "level"),
                                rarity == null ? null : Rarity.valueOf(rarity)));
            }
            default -> throw new IllegalArgumentException("Unknown Starlyn shop kind " + entry.get("kind"));
        };
    }

    private static ItemType item(Object name) {
        ItemType type = ItemType.get((String) name);
        if (type == null) throw new IllegalArgumentException("Unknown item " + name);
        return type;
    }

    private static Material material(Object name) {
        return Material.fromKey("minecraft:" + ((String) name).toLowerCase(Locale.ROOT));
    }

    private static Map<String, Object> read(Path file) {
        try (InputStream stream = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(stream);
            return root == null ? Map.of() : root;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + file, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value == null ? Map.of() : (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        return value == null ? List.of() : (List<Map<String, Object>>) value;
    }

    private static List<Integer> integers(Object value) {
        List<Integer> result = new ArrayList<>();
        if (value instanceof List<?> list) for (Object element : list) result.add(((Number) element).intValue());
        return List.copyOf(result);
    }

    private static int integer(Map<String, Object> map, String key) {
        return map.get(key) instanceof Number number ? number.intValue() : 0;
    }

    private static long longValue(Map<String, Object> map, String key) {
        return map.get(key) instanceof Number number ? number.longValue() : 0;
    }

    private static double decimal(Map<String, Object> map, String key) {
        return map.get(key) instanceof Number number ? number.doubleValue() : 0;
    }
}
