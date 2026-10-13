package net.swofty.type.skyblockgeneric.garden.pest;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
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

public final class PestCatalogue {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/pests.json");
    private static PestCatalogue instance;

    public record Universal(int coins, int farmingXp, int gardenXp, int bits, List<String> oneOf, double oneOfChance) {
    }

    public record Pest(String id, String name, @Nullable GardenCrop crop, int level, @Nullable String spray,
                       EntityType entity, @Nullable String item, @Nullable String altItem, int base, double perFortune,
                       boolean flees, @Nullable String vinyl, List<MobDrop> rare) {
        public boolean isTrapOnly() {
            return spray == null;
        }
    }

    private final int health;
    private final Universal universal;
    private final Map<String, Pest> pests = new LinkedHashMap<>();

    private PestCatalogue(JSONObject json) {
        health = json.getInt("health");
        JSONObject all = json.getJSONObject("universal");
        universal = new Universal(all.getInt("coins"), all.getInt("farmingXp"), all.getInt("gardenXp"), all.getInt("bits"),
                strings(all.getJSONArray("oneOf")), all.getDouble("oneOfChanceEach"));
        JSONArray array = json.getJSONArray("pests");
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            List<MobDrop> rare = rare(entry);
            String vinyl = PestVinyls.vinylOf(entry.getString("id"));
            if (vinyl != null) rare.add(MobDrop.chance(vinyl, PestVinyls.DROP_CHANCE_PERCENT));
            pests.put(entry.getString("id"), new Pest(entry.getString("id"), entry.getString("name"),
                    GardenCrop.valueOf(entry.getString("crop")), entry.getInt("level"), entry.getString("spray"),
                    entity(entry.getString("entity")), entry.getString("item"), entry.optString("altItem", null),
                    entry.getInt("base"), entry.getDouble("perFortune"), entry.optBoolean("flees", true), PestVinyls.vinylOf(entry.getString("id")), rare));
        }
        JSONObject traps = json.getJSONObject("traps");
        for (String id : traps.keySet()) {
            JSONObject entry = traps.getJSONObject(id);
            pests.put(id, new Pest(id, entry.getString("name"), null, 5, null, entity(entry.getString("entity")),
                    null, null, 0, 1, true, null, rare(entry)));
        }
    }

    public static synchronized PestCatalogue get() {
        if (instance == null) {
            try {
                instance = new PestCatalogue(new JSONObject(Files.readString(FILE)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance;
    }

    public int health() {
        return health;
    }

    public Universal universal() {
        return universal;
    }

    public @Nullable Pest pest(String id) {
        return pests.get(id);
    }

    public List<Pest> natural() {
        return pests.values().stream().filter(pest -> !pest.isTrapOnly()).toList();
    }

    public List<Pest> all() {
        return List.copyOf(pests.values());
    }

    private static List<MobDrop> rare(JSONObject entry) {
        List<MobDrop> drops = new ArrayList<>();
        JSONArray items = entry.optJSONArray("rare");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONArray row = items.getJSONArray(i);
                drops.add(MobDrop.chance(row.getString(0), row.optInt(2, 1), row.getDouble(1)));
            }
        }
        JSONArray pets = entry.optJSONArray("pets");
        if (pets != null) {
            for (int i = 0; i < pets.length(); i++) {
                JSONArray row = pets.getJSONArray(i);
                drops.add(MobDrop.pet(row.getString(0), Rarity.valueOf(row.getString(1)), row.getDouble(2)));
            }
        }
        JSONArray books = entry.optJSONArray("books");
        if (books != null) {
            for (int i = 0; i < books.length(); i++) {
                JSONArray row = books.getJSONArray(i);
                drops.add(MobDrop.book(row.getString(0), row.getInt(1), row.getDouble(2)));
            }
        }
        return drops;
    }

    private static EntityType entity(String name) {
        return EntityType.fromKey("minecraft:" + name.toLowerCase());
    }

    private static List<String> strings(JSONArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) list.add(array.getString(i));
        return list;
    }
}
