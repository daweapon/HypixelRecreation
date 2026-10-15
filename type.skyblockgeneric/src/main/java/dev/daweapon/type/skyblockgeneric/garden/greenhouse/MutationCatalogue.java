package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MutationCatalogue {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/mutations.json");
    private static MutationCatalogue instance;

    public record Condition(String crop, int count, boolean mutation) {
    }

    public record Drop(String item, int amount) {
    }

    public record Mutation(String id, @Nullable String itemId, String name, String rarity, int stages, String surface, String size,
                           List<GreenhouseEffect> effects, String mode, List<Condition> conditions, @Nullable String note,
                           double chancePercent, int farmingXp, List<Drop> drops, int copperRepeat, int copperFirst, int coinsCost,
                           @Nullable String behaviour, @Nullable String decay, List<String> tooltip) {
        public @Nullable ItemType item() {
            return itemId == null ? null : ItemType.get(itemId);
        }

        public int footprint() {
            return Footprint.sizeOf(size);
        }

        public boolean neverDecays() {
            return "none".equalsIgnoreCase(decay);
        }
    }

    private final Map<String, Mutation> mutations = new LinkedHashMap<>();
    private final Map<String, Mutation> byItem = new LinkedHashMap<>();

    private MutationCatalogue(JSONObject json) {
        JSONArray array = json.getJSONArray("mutations");
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            if (entry.optBoolean("special", false)) continue;
            List<GreenhouseEffect> effects = new ArrayList<>();
            JSONArray effectArray = entry.optJSONArray("effects");
            if (effectArray != null) {
                for (int e = 0; e < effectArray.length(); e++) {
                    try {
                        effects.add(GreenhouseEffect.valueOf(effectArray.getString(e)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            List<Condition> conditions = new ArrayList<>();
            JSONArray conditionArray = entry.optJSONArray("conditions");
            if (conditionArray != null) {
                for (int c = 0; c < conditionArray.length(); c++) {
                    JSONObject condition = conditionArray.getJSONObject(c);
                    conditions.add(new Condition(condition.getString("crop"), condition.getInt("count"), "MUTATION".equals(condition.optString("kind"))));
                }
            }
            List<Drop> drops = new ArrayList<>();
            JSONArray dropArray = entry.optJSONArray("drops");
            if (dropArray != null) {
                for (int d = 0; d < dropArray.length(); d++) {
                    JSONObject drop = dropArray.getJSONObject(d);
                    drops.add(new Drop(drop.getString("item"), drop.optInt("min", 1)));
                }
            }
            List<String> tooltip = new ArrayList<>();
            JSONArray tooltipArray = entry.optJSONArray("tooltip");
            if (tooltipArray != null) for (int t = 0; t < tooltipArray.length(); t++) tooltip.add(tooltipArray.getString(t));
            JSONObject copper = entry.optJSONObject("copper");
            JSONArray surfaceArray = entry.optJSONArray("surfaces");
            List<String> surfaceNames = new ArrayList<>();
            if (surfaceArray != null) for (int s = 0; s < surfaceArray.length(); s++) surfaceNames.add(surfaceArray.getString(s));
            String surface = surfaceNames.isEmpty() ? entry.optString("surface", "Farmland") : String.join(" / ", surfaceNames);
            Mutation mutation = new Mutation(entry.getString("id"), entry.isNull("itemId") ? null : entry.optString("itemId", null),
                    entry.getString("name"), entry.optString("rarity", "COMMON"), entry.optInt("stages", 0),
                    surface, entry.optString("size", "1x1"), effects, entry.optString("mode", "ADJACENT_ALL"),
                    conditions, entry.isNull("specialNote") ? null : entry.optString("specialNote", null), entry.optDouble("chancePercent", 0),
                    entry.optInt("farmingXp", 0), drops, copper == null ? 0 : copper.optInt("analyzeRepeat", 0),
                    copper == null ? 0 : copper.optInt("analyzeFirst", 0), copper == null ? 0 : copper.optInt("coinsCost", 0),
                    entry.isNull("behaviour") ? null : entry.optString("behaviour", null),
                    entry.isNull("decay") ? null : entry.optString("decay", null), tooltip);
            mutations.put(mutation.id(), mutation);
            if (mutation.itemId() != null) byItem.put(mutation.itemId(), mutation);
        }
    }

    public static synchronized MutationCatalogue get() {
        if (instance == null) {
            try {
                instance = new MutationCatalogue(new JSONObject(Files.readString(FILE)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance;
    }

    public void registerInto(GreenhouseConfig config) {
        for (Mutation mutation : mutations.values()) {
            config.register(new PlantType(mutation.id(), mutation.name(), null, true, mutation.stages(), 0, mutation.effects(),
                    mutation.surface(), mutation.rarity()));
        }
    }

    public @Nullable Mutation mutation(String id) {
        return mutations.get(id);
    }

    public @Nullable Mutation ofItem(@Nullable ItemType item) {
        return item == null ? null : byItem.get(item.name());
    }

    public List<Mutation> all() {
        return List.copyOf(mutations.values());
    }
}
