package dev.daweapon.type.catacombs.engine.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Every dungeon mob's captured look, read once from {@code configuration/skyblock/dungeons/mob_appearance.json}. */
public final class MobLooks {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/mob_appearance.json");
    private static final Map<String, EquipmentSlot> SLOTS = Map.of(
            "head", EquipmentSlot.HELMET,
            "chest", EquipmentSlot.CHESTPLATE,
            "legs", EquipmentSlot.LEGGINGS,
            "feet", EquipmentSlot.BOOTS,
            "mainhand", EquipmentSlot.MAIN_HAND);
    private static volatile MobLooks shared;

    /** The Watcher's summons: a name with its fixed skin, random health and random vanilla gear per spawn. */
    public record WatcherMobs(List<MobType> types, String nameColour, int minHealth, int maxHealth, int healthStep,
                              List<Map.Entry<String, Skin>> names, Map<EquipmentSlot, List<Optional<ItemSpec>>> gear) {

        public record Summon(MobLook look, long health) {
        }

        public Summon roll(RandomGenerator random) {
            Map.Entry<String, Skin> name = names.get(random.nextInt(names.size()));
            Map<EquipmentSlot, ItemSpec> equipment = new EnumMap<>(EquipmentSlot.class);
            gear.forEach((slot, pool) -> pool.get(random.nextInt(pool.size())).ifPresent(spec -> equipment.put(slot, spec)));
            int steps = (maxHealth - minHealth) / healthStep;
            long health = minHealth + (long) random.nextInt(steps + 1) * healthStep;
            MobLook look = new MobLook(EntityType.MANNEQUIN, types, nameColour, false, name.getKey(), null, false, null,
                    List.of(new MobLook.Outfit(name.getValue(), equipment)));
            return new Summon(look, health);
        }
    }

    private final Map<String, MobLook> mobs;
    private final @Nullable WatcherMobs watcherMobs;

    private MobLooks(Map<String, MobLook> mobs, @Nullable WatcherMobs watcherMobs) {
        this.mobs = mobs;
        this.watcherMobs = watcherMobs;
    }

    public static MobLooks shared() {
        MobLooks loaded = shared;
        if (loaded != null) return loaded;
        synchronized (MobLooks.class) {
            if (shared == null) shared = load(FILE);
            return shared;
        }
    }

    public static MobLooks load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the dungeon mob looks from {}", file);
            return new MobLooks(Map.of(), null);
        }
    }

    public static MobLooks parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Map<String, MobLook> mobs = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("mobs").entrySet()) {
            mobs.put(entry.getKey(), look(entry.getValue().getAsJsonObject()));
        }
        WatcherMobs watcher = root.has("watcherMobs") ? watcherMobs(root.getAsJsonObject("watcherMobs")) : null;
        return new MobLooks(Map.copyOf(mobs), watcher);
    }

    private static MobLook look(JsonObject json) {
        List<MobLook.Outfit> outfits = new ArrayList<>();
        for (JsonElement variant : json.getAsJsonArray("variants")) {
            JsonObject outfit = variant.getAsJsonObject();
            Skin skin = outfit.has("skin") ? Skin.parse(outfit.getAsJsonObject("skin")) : null;
            Map<EquipmentSlot, ItemSpec> equipment = new EnumMap<>(EquipmentSlot.class);
            JsonObject gear = outfit.getAsJsonObject("equipment");
            for (Map.Entry<String, JsonElement> piece : gear.entrySet()) {
                equipment.put(slot(piece.getKey()), ItemSpec.parse(piece.getValue().getAsJsonObject()));
            }
            outfits.add(new MobLook.Outfit(skin, equipment));
        }
        return new MobLook(entity(json.get("entity").getAsString()), types(json.getAsJsonArray("types")),
                json.has("nameColour") ? json.get("nameColour").getAsString() : "<c>",
                json.has("bold") && json.get("bold").getAsBoolean(),
                json.has("displayName") ? json.get("displayName").getAsString() : null,
                json.has("level") ? json.get("level").getAsInt() : null,
                json.has("showMaxHealth") && json.get("showMaxHealth").getAsBoolean(),
                json.has("header") ? json.get("header").getAsString() : null,
                outfits);
    }

    private static WatcherMobs watcherMobs(JsonObject json) {
        JsonObject health = json.getAsJsonObject("health");
        List<Map.Entry<String, Skin>> names = new ArrayList<>();
        for (JsonElement entry : json.getAsJsonArray("names")) {
            JsonObject name = entry.getAsJsonObject();
            names.add(Map.entry(name.get("name").getAsString(), Skin.parse(name.getAsJsonObject("skin"))));
        }
        Map<EquipmentSlot, List<Optional<ItemSpec>>> gear = new EnumMap<>(EquipmentSlot.class);
        for (String slot : List.of("chest", "legs", "feet", "mainhand")) {
            List<Optional<ItemSpec>> pool = new ArrayList<>();
            for (JsonElement piece : json.getAsJsonArray(slot)) {
                pool.add(piece.isJsonNull() ? Optional.empty() : Optional.of(ItemSpec.parse(piece.getAsJsonObject())));
            }
            gear.put(slot(slot), List.copyOf(pool));
        }
        return new WatcherMobs(types(json.getAsJsonArray("types")), json.get("nameColour").getAsString(),
                health.get("min").getAsInt(), health.get("max").getAsInt(), health.get("step").getAsInt(), List.copyOf(names), gear);
    }

    private static EquipmentSlot slot(String name) {
        EquipmentSlot slot = SLOTS.get(name);
        if (slot == null) throw new IllegalArgumentException("Unknown equipment slot " + name);
        return slot;
    }

    private static EntityType entity(String name) {
        return switch (name) {
            case "PLAYER" -> EntityType.MANNEQUIN;
            case "ZOMBIE" -> EntityType.ZOMBIE;
            case "SKELETON" -> EntityType.SKELETON;
            default -> throw new IllegalArgumentException("Unknown dungeon mob entity " + name);
        };
    }

    private static List<MobType> types(JsonArray array) {
        List<MobType> types = new ArrayList<>();
        array.forEach(type -> types.add(MobType.valueOf(type.getAsString())));
        return types;
    }

    public Optional<MobLook> look(String mobId) {
        return Optional.ofNullable(mobs.get(mobId));
    }

    public Map<String, MobLook> all() {
        return mobs;
    }

    public Optional<WatcherMobs> watcherMobs() {
        return Optional.ofNullable(watcherMobs);
    }
}
