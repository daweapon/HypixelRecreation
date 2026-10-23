package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;

import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ModelCollector {
    private static Path directory;
    private static final Set<String> done = new HashSet<>();
    private static int exported;

    private ModelCollector() {
    }

    public static synchronized void use(Path target) {
        directory = target;
        done.clear();
        exported = 0;
    }

    public static synchronized String captureNearby(double radius) throws java.io.IOException {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return "No world is loaded.";
        }
        Path previous = directory;
        Path target = client.gameDirectory.toPath().resolve("model-captures")
                .resolve(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        Files.createDirectories(target);
        use(target);
        com.google.gson.JsonArray entities = new com.google.gson.JsonArray();
        try {
            item(client.player.getMainHandItem());
            for (net.minecraft.world.entity.Entity entity : client.level.entitiesForRendering()) {
                if (entity != client.player && entity.distanceTo(client.player) <= radius) {
                    entities.add(EntityTracker.detail(entity));
                }
            }
            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            root.add("entities", entities);
            Files.writeString(target.resolve("entities.json"),
                    new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            return target + " (" + entities.size() + " entities, " + exported + " model files)";
        } finally {
            if (previous != null) {
                use(previous);
            } else {
                release();
            }
        }
    }

    public static synchronized void release() {
        directory = null;
    }

    public static synchronized int exported() {
        return exported;
    }

    public static void item(ItemStack stack) {
        if (directory == null || stack == null || stack.isEmpty()) {
            return;
        }
        Identifier model = stack.get(DataComponents.ITEM_MODEL);
        if (model != null) {
            itemModel(model.toString());
        }
    }

    public static void scanJson(JsonElement element) {
        if (directory == null || element == null) {
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("minecraft:item_model") && entry.getValue().isJsonPrimitive()) {
                    itemModel(entry.getValue().getAsString());
                } else if (entry.getKey().equals("block_state") && entry.getValue().isJsonObject()
                        && entry.getValue().getAsJsonObject().has("Name")) {
                    blockState(entry.getValue().getAsJsonObject().get("Name").getAsString());
                } else {
                    scanJson(entry.getValue());
                }
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(ModelCollector::scanJson);
        }
    }

    public static synchronized void itemModel(String id) {
        if (directory == null || !done.add("item:" + id)) {
            return;
        }
        Identifier identifier = Identifier.parse(id);
        String path = "items/" + identifier.getPath() + ".json";
        JsonElement definition = copyJson(identifier.getNamespace(), path);
        if (definition != null) {
            walkModelReferences(definition, identifier.getNamespace());
        } else {
            model(id);
        }
    }

    public static synchronized void blockState(String id) {
        Identifier identifier = Identifier.parse(id);
        if (identifier.getNamespace().equals("minecraft") || directory == null || !done.add("state:" + id)) {
            return;
        }
        JsonElement state = copyJson(identifier.getNamespace(), "blockstates/" + identifier.getPath() + ".json");
        if (state != null) {
            walkModelReferences(state, identifier.getNamespace());
        }
    }

    private static void walkModelReferences(JsonElement element, String namespace) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("model") && entry.getValue().isJsonPrimitive()) {
                    model(entry.getValue().getAsString());
                } else {
                    walkModelReferences(entry.getValue(), namespace);
                }
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> walkModelReferences(child, namespace));
        }
    }

    private static void model(String id) {
        Identifier identifier = Identifier.parse(id);
        if (!done.add("model:" + identifier)) {
            return;
        }
        JsonElement json = copyJson(identifier.getNamespace(), "models/" + identifier.getPath() + ".json");
        if (json == null || !json.isJsonObject()) {
            return;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("parent") && object.get("parent").isJsonPrimitive()) {
            String parent = object.get("parent").getAsString();
            if (!parent.startsWith("builtin/") && !parent.startsWith("minecraft:builtin/")) {
                model(parent);
            }
        }
        if (object.has("textures") && object.get("textures").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("textures").entrySet()) {
                if (entry.getValue().isJsonPrimitive() && !entry.getValue().getAsString().startsWith("#")) {
                    Identifier texture = Identifier.parse(entry.getValue().getAsString());
                    copyBinary(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
                    copyBinary(texture.getNamespace(), "textures/" + texture.getPath() + ".png.mcmeta");
                }
            }
        }
        if (object.has("overrides") && object.get("overrides").isJsonArray()) {
            walkModelReferences(object.get("overrides"), identifier.getNamespace());
        }
    }

    private static JsonElement copyJson(String namespace, String path) {
        Optional<Resource> resource = find(namespace, path);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = resource.get().open(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            String text = new String(stream == null ? new byte[0] : readAll(reader));
            write(namespace, path, text.getBytes(StandardCharsets.UTF_8));
            return JsonParser.parseString(text);
        } catch (Exception exception) {
            return null;
        }
    }

    private static byte[] readAll(Reader reader) throws java.io.IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void copyBinary(String namespace, String path) {
        if (!done.add("file:" + namespace + ":" + path)) {
            return;
        }
        Optional<Resource> resource = find(namespace, path);
        if (resource.isEmpty()) {
            return;
        }
        try (InputStream stream = resource.get().open()) {
            write(namespace, path, stream.readAllBytes());
        } catch (Exception ignored) {
        }
    }

    private static Optional<Resource> find(String namespace, String path) {
        return Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static void write(String namespace, String path, byte[] bytes) throws java.io.IOException {
        Path target = directory.resolve("assets").resolve(namespace).resolve(path);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        exported++;
    }
}
