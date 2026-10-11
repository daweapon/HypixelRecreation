package net.swofty.codegen;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class ItemTypeGenerator {
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&()*+,-./:;<=>?@[]^_{|}~";
    private static final String PACKAGE = "net.swofty.commons.skyblock.item";
    private static final String CONSTANTS_PREFIX = "ItemTypeConstants";
    private static final int CHUNK_SIZE = 1000;
    private static final String HEADER = "// AUTO-GENERATED FILE. DO NOT EDIT.\npackage " + PACKAGE + ";\n\n";
    static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected args: <inputDir> <outputDir>");
        }

        Path inputDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);

        Map<String, ItemYaml> items = new TreeMap<>();

        for (Path file : findYamlFiles(inputDir)) {
            for (ItemYaml item : loadItems(file)) {
                if (item.id() == null || item.id().isBlank()) continue;
                items.put(item.id(), item);
            }
        }

        generateItemTypes(items.values(), outputDir);
    }

    static List<Path> findYamlFiles(Path root) throws IOException {
        if (!Files.exists(root)) return List.of();
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                    .toList();
        }
    }

    static List<ItemYaml> loadItems(Path file) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(Files.newInputStream(file));
        if (root == null) return List.of();

        Object rawItems = root.get("items");
        if (!(rawItems instanceof List<?> list)) return List.of();

        List<ItemYaml> result = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> item)) continue;
            Object id = item.get("id");
            Object rarity = item.get("rarity");
            Object material = item.get("material");
            if (!(id instanceof String)) continue;
            result.add(new ItemYaml(
                    (String) id,
                    material instanceof String ? (String) material : "BARRIER",
                    rarity instanceof String ? (String) rarity : null
            ));
        }
        return result;
    }

    static void generateItemTypes(
            Collection<ItemYaml> items,
            Path outputDir
    ) throws IOException {
        Path packageDir = outputDir.resolve(PACKAGE.replace('.', '/'));
        Files.createDirectories(packageDir);
        try (Stream<Path> stale = Files.list(packageDir)) {
            for (Path file : stale.filter(f -> f.getFileName().toString().startsWith(CONSTANTS_PREFIX)).toList()) {
                Files.delete(file);
            }
        }

        List<String> names = new ArrayList<>();
        List<String> materials = new ArrayList<>();
        List<String> rarities = new ArrayList<>();
        StringBuilder table = new StringBuilder();
        for (ItemYaml item : items) {
            names.add(toEnumConstantName(item.id()));
            int materialIndex = indexOf(materials, item.material().toUpperCase(Locale.ROOT));
            table.append(ALPHABET.charAt(materialIndex / ALPHABET.length()))
                    .append(ALPHABET.charAt(materialIndex % ALPHABET.length()))
                    .append(ALPHABET.charAt(indexOf(rarities, resolveRarity(item))));
        }

        int chunks = (names.size() + CHUNK_SIZE - 1) / CHUNK_SIZE;
        for (int chunk = 0; chunk < chunks; chunk++) {
            StringBuilder body = new StringBuilder();
            for (int ordinal = chunk * CHUNK_SIZE; ordinal < Math.min(names.size(), (chunk + 1) * CHUNK_SIZE); ordinal++) {
                body.append("    ItemType ").append(names.get(ordinal)).append(" = new ItemType(\"")
                        .append(names.get(ordinal)).append("\", ").append(ordinal).append(");\n");
            }
            Files.writeString(packageDir.resolve(CONSTANTS_PREFIX + chunk + ".java"), HEADER
                    + "public interface " + CONSTANTS_PREFIX + chunk + " {\n" + body + "}\n");
        }

        List<String> interfaces = new ArrayList<>();
        StringBuilder loadAll = new StringBuilder();
        for (int chunk = 0; chunk < chunks; chunk++) {
            interfaces.add(CONSTANTS_PREFIX + chunk);
            loadAll.append("        Objects.requireNonNull(").append(CONSTANTS_PREFIX).append(chunk).append('.')
                    .append(names.get(chunk * CHUNK_SIZE)).append(");\n");
        }

        String source = HEADER + """
                import java.util.HashMap;
                import java.util.Map;
                import java.util.Objects;
                import net.minestom.server.item.Material;
                import net.swofty.commons.StringUtility;
                import org.jetbrains.annotations.Nullable;

                /**
                 * Every SkyBlock item. This is a class, not an enum: an enum builds every constant in one static
                 * initializer, which the JVM caps at 64 KB (about 4,000 constants). The constants are declared
                 * %1$d per interface in %2$sN, each with its own initializer, and inherited from here, so
                 * {@code ItemType.X}, {@code ==}, name(), ordinal(), values() and valueOf() behave as they did.
                 */
                public final class ItemType implements Comparable<ItemType>, %3$s {
                    private static final ItemType[] VALUES = new ItemType[%4$d];
                    private static volatile Map<String, ItemType> byName;

                    public final Material material;
                    public final Rarity rarity;
                    private final String name;
                    private final int ordinal;

                    ItemType(String name, int ordinal) {
                        this.name = name;
                        this.ordinal = ordinal;
                        int at = 3 * ordinal;
                        this.material = Defaults.MATERIALS[Defaults.index(at) * Defaults.ALPHABET.length() + Defaults.index(at + 1)];
                        this.rarity = Defaults.RARITIES[Defaults.index(at + 2)];
                        VALUES[ordinal] = this;
                    }

                    public String name() {
                        return name;
                    }

                    public int ordinal() {
                        return ordinal;
                    }

                    @Override
                    public String toString() {
                        return name;
                    }

                    @Override
                    public int compareTo(ItemType other) {
                        return Integer.compare(ordinal, other.ordinal);
                    }

                    public static ItemType[] values() {
                        loadAll();
                        return VALUES.clone();
                    }

                    public static ItemType valueOf(String name) {
                        Objects.requireNonNull(name, "Name is null");
                        ItemType type = byName().get(name);
                        if (type == null) throw new IllegalArgumentException("No ItemType " + name);
                        return type;
                    }

                    public static @Nullable ItemType get(String name) {
                        if (name == null) return null;
                        return byName().get(name.replace("minecraft:", "").toUpperCase());
                    }

                    public static boolean isVanillaReplaced(String item) {
                        return get(item) != null;
                    }

                    public static @Nullable ItemType fromMaterial(Material material) {
                        String formattedItemName = StringUtility.toNormalCase(material.key().value());
                        for (ItemType itemType : values()) {
                            if (itemType.material == material && formattedItemName.equals(StringUtility.toNormalCase(itemType.getDisplayName()))) {
                                return itemType;
                            }
                        }
                        return null;
                    }

                    public String getDisplayName() {
                        return StringUtility.toNormalCase(name);
                    }

                    private static Map<String, ItemType> byName() {
                        Map<String, ItemType> map = byName;
                        if (map == null) {
                            map = new HashMap<>();
                            for (ItemType type : values()) map.put(type.name, type);
                            byName = map;
                        }
                        return map;
                    }

                    private static void loadAll() {
                %5$s    }

                    // Each constant's material and rarity are three characters of TABLE: two for its index in
                    // MATERIALS, one for its index in RARITIES.
                    private static final class Defaults {
                        static final Material[] MATERIALS = {%6$s};
                        static final Rarity[] RARITIES = {%7$s};
                        static final String ALPHABET = "%8$s";
                        static final String TABLE = "%9$s";

                        static int index(int at) {
                            return ALPHABET.indexOf(TABLE.charAt(at));
                        }
                    }
                }
                """.formatted(CHUNK_SIZE, CONSTANTS_PREFIX, String.join(", ", interfaces), names.size(),
                loadAll,
                String.join(", ", materials.stream().map(m -> "Material." + m).toList()),
                String.join(", ", rarities.stream().map(r -> "Rarity." + r).toList()),
                ALPHABET, table);
        Files.writeString(packageDir.resolve("ItemType.java"), source);
    }

    private static int indexOf(List<String> values, String value) {
        int index = values.indexOf(value);
        if (index >= 0) return index;
        values.add(value);
        return values.size() - 1;
    }

    private static String resolveRarity(ItemYaml item) {
        String rarityString = item.rarity();
        if (rarityString == null || rarityString.isBlank()) return "COMMON";
        return rarityString.trim().toUpperCase(Locale.ROOT);
    }

    private static String toEnumConstantName(String id) {
        if (id == null) return "UNKNOWN";
        String name = id;
        int idx = name.indexOf(':');
        if (idx >= 0) name = name.substring(idx + 1);
        name = name.trim().toUpperCase(Locale.ROOT);
        name = name.replace('-', '_');
        name = name.replaceAll("[^A-Z0-9_]", "_");
        if (name.isEmpty()) name = "UNKNOWN";
        if (Character.isDigit(name.charAt(0))) name = "_" + name;
        return name;
    }

    private record ItemYaml(String id, String material, String rarity) {
    }
}
