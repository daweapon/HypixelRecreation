package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The essence perk shops that are data driven: Wither, Undead, Spider, Dragon, Gold, Diamond, Forest,
 * Sun Gecko and Safari. Perks live in configuration/skyblock/essence_shops.json. Ice, Crimson and
 * Fossil keep their own classes because their effects are coded individually.
 */
public final class EssenceShops {
    private static final Path FILE = Path.of("./configuration/skyblock/essence_shops.json");
    private static Map<EssenceType, EssenceShop<CataloguePerk>> shops;
    private static Map<EssenceType, String> sellers;

    private EssenceShops() {
    }

    public static Optional<EssenceShop<CataloguePerk>> shop(EssenceType type) {
        return Optional.ofNullable(shops().get(type));
    }

    public static Map<EssenceType, EssenceShop<CataloguePerk>> shops() {
        if (shops == null) load(FILE);
        return shops;
    }

    public static String seller(EssenceType type) {
        shops();
        return sellers.getOrDefault(type, "");
    }

    public static void maxAvailable(SkyBlockPlayer player) {
        shops().values().forEach(shop -> shop.maxAvailable(player));
    }

    public static synchronized void load(Path path) {
        Map<EssenceType, EssenceShop<CataloguePerk>> loaded = new EnumMap<>(EssenceType.class);
        Map<EssenceType, String> loadedSellers = new EnumMap<>(EssenceType.class);
        try {
            JSONObject root = new JSONObject(Files.readString(path));
            for (String key : root.keySet()) {
                EssenceType type = EssenceType.valueOf(key);
                JSONObject entry = root.getJSONObject(key);
                loaded.put(type, parse(type, entry));
                loadedSellers.put(type, entry.optString("seller", ""));
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to load essence shops from {}", path);
        }
        shops = loaded;
        sellers = loadedSellers;
    }

    private static EssenceShop<CataloguePerk> parse(EssenceType type, JSONObject entry) {
        int total = entry.optInt("totalXP", 0);
        JSONArray array = entry.getJSONArray("perks");
        List<CataloguePerk> perks = new ArrayList<>();
        List<int[]> explicit = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject perk = array.getJSONObject(i);
            long[] costs = longs(perk.getJSONArray("costs"));
            double[] values = perk.has("values") ? doubles(perk.getJSONArray("values")) : new double[]{1};
            explicit.add(perk.has("xp") ? ints(perk.getJSONArray("xp")) : null);
            perks.add(new CataloguePerk(type, perk.getString("id"), perk.getString("name"),
                    perk.getString("description"), perk.optString("icon", "PAPER"), values, costs,
                    perk.has("labels") ? strings(perk.getJSONArray("labels")) : null,
                    perk.has("stat") ? ItemStatistic.valueOf(perk.getString("stat")) : null,
                    CataloguePerk.Scope.valueOf(perk.optString("scope", "ANYWHERE")),
                    perk.isNull("unavailable") ? null : perk.optString("unavailable", null)));
        }
        Map<CataloguePerk, int[]> tierXP = new LinkedHashMap<>();
        int[][] split = splitXP(total, perks, explicit);
        for (int i = 0; i < perks.size(); i++) tierXP.put(perks.get(i), split[i]);
        return new EssenceShop<>(type, List.copyOf(perks), tierXP, xpSource(type));
    }

    /**
     * Hypixel only gives each shop's SkyBlock XP total, so perks without an explicit split share the
     * total in proportion to their tier count, rising with each tier.
     */
    static int[][] splitXP(int total, List<CataloguePerk> perks, List<int[]> explicit) {
        int[][] result = new int[perks.size()][];
        int fixed = 0;
        int weights = 0;
        for (int i = 0; i < perks.size(); i++) {
            if (explicit.get(i) != null) {
                result[i] = explicit.get(i);
                for (int xp : result[i]) fixed += xp;
            } else {
                weights += perks.get(i).getMaxTier() * (perks.get(i).getMaxTier() + 1) / 2;
            }
        }
        int remaining = Math.max(0, total - fixed);
        int handedOut = 0;
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < perks.size(); i++) {
            if (result[i] != null) continue;
            int tiers = perks.get(i).getMaxTier();
            result[i] = new int[tiers];
            open.add(i);
            for (int tier = 0; tier < tiers; tier++) {
                int xp = (int) Math.floor((double) remaining * (tier + 1) / weights);
                result[i][tier] = xp;
                handedOut += xp;
            }
        }
        for (int leftover = remaining - handedOut, at = 0; leftover > 0 && !open.isEmpty(); leftover--, at++) {
            int[] tiers = result[open.get(at % open.size())];
            tiers[tiers.length - 1 - (at / open.size()) % tiers.length]++;
        }
        return result;
    }

    public static SkyBlockXPSource xpSource(EssenceType type) {
        return switch (type) {
            case WITHER -> SkyBlockXPSource.WITHER_ESSENCE_SHOP;
            case UNDEAD -> SkyBlockXPSource.UNDEAD_ESSENCE_SHOP;
            case SPIDER -> SkyBlockXPSource.SPIDER_ESSENCE_SHOP;
            case DRAGON -> SkyBlockXPSource.DRAGON_ESSENCE_SHOP;
            case GOLD -> SkyBlockXPSource.GOLD_ESSENCE_SHOP;
            case DIAMOND -> SkyBlockXPSource.DIAMOND_ESSENCE_SHOP;
            case FOREST -> SkyBlockXPSource.FOREST_ESSENCE_SHOP;
            case SAFARI -> SkyBlockXPSource.SAFARI_ESSENCE_SHOP;
            default -> null;
        };
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        Map<ItemStatistic, Double> totals = new EnumMap<>(ItemStatistic.class);
        for (EssenceShop<CataloguePerk> shop : shops().values()) {
            for (CataloguePerk perk : shop.perks()) {
                if (perk.stat() == null || !inScope(perk.scope())) continue;
                double value = EssencePerks.value(player, perk);
                if (value != 0) totals.merge(perk.stat(), value, Double::sum);
            }
        }
        if (totals.isEmpty()) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        totals.forEach(builder::withBase);
        return builder.build();
    }

    static boolean inScope(CataloguePerk.Scope scope) {
        if (scope == CataloguePerk.Scope.ANYWHERE) return true;
        if (HypixelConst.getTypeLoader() == null) return false;
        ServerType server = HypixelConst.getTypeLoader().getType();
        return switch (scope) {
            case MINING -> server == ServerType.SKYBLOCK_DWARVEN_MINES || server == ServerType.SKYBLOCK_CRYSTAL_HOLLOWS;
            case SPIDERS_DEN -> server == ServerType.SKYBLOCK_SPIDERS_DEN;
            case THE_END -> server == ServerType.SKYBLOCK_THE_END;
            case FORAGING -> server == ServerType.SKYBLOCK_GALATEA;
            case ANYWHERE -> true;
        };
    }

    private static String[] strings(JSONArray array) {
        String[] values = new String[array.length()];
        for (int i = 0; i < values.length; i++) values[i] = array.getString(i);
        return values;
    }

    private static long[] longs(JSONArray array) {
        long[] values = new long[array.length()];
        for (int i = 0; i < values.length; i++) values[i] = array.getLong(i);
        return values;
    }

    private static double[] doubles(JSONArray array) {
        double[] values = new double[array.length()];
        for (int i = 0; i < values.length; i++) values[i] = array.getDouble(i);
        return values;
    }

    private static int[] ints(JSONArray array) {
        int[] values = new int[array.length()];
        for (int i = 0; i < values.length; i++) values[i] = array.getInt(i);
        return values;
    }
}
