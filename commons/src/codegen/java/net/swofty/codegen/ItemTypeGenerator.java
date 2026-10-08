package net.swofty.codegen;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.element.Modifier;
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

        generateEnum(items.values(), outputDir);
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

    static void generateEnum(
            Collection<ItemYaml> items,
            Path outputDir
    ) throws IOException {

        ClassName material = ClassName.get("net.minestom.server.item", "Material");
        ClassName rarity = ClassName.get("net.swofty.commons.skyblock.item", "Rarity");
        ClassName nullable = ClassName.get("org.jetbrains.annotations", "Nullable");

        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder("ItemType")
                .addModifiers(Modifier.PUBLIC);

        List<String> materials = new ArrayList<>();
        List<String> rarities = new ArrayList<>();
        StringBuilder table = new StringBuilder();
        for (ItemYaml item : items) {
            enumBuilder.addEnumConstant(toEnumConstantName(item.id()));
            int materialIndex = indexOf(materials, item.material().toUpperCase(Locale.ROOT));
            table.append(ALPHABET.charAt(materialIndex / ALPHABET.length()))
                    .append(ALPHABET.charAt(materialIndex % ALPHABET.length()))
                    .append(ALPHABET.charAt(indexOf(rarities, resolveRarity(item))));
        }

        enumBuilder.addField(material, "material",
                Modifier.PUBLIC, Modifier.FINAL);
        enumBuilder.addField(rarity, "rarity",
                Modifier.PUBLIC, Modifier.FINAL);

        // Constants take no arguments because every argument adds bytecode to the enum's static
        // initializer, which the JVM caps at 64 KB. Each constant's material and rarity are three
        // characters of TABLE: two for its index in MATERIALS, one for its index in RARITIES.
        enumBuilder.addType(TypeSpec.classBuilder("Defaults")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addField(FieldSpec.builder(ArrayTypeName.of(material), "MATERIALS", Modifier.STATIC, Modifier.FINAL)
                        .initializer("{$L}", String.join(", ", materials.stream().map(m -> "Material." + m).toList()))
                        .build())
                .addField(FieldSpec.builder(ArrayTypeName.of(rarity), "RARITIES", Modifier.STATIC, Modifier.FINAL)
                        .initializer("{$L}", String.join(", ", rarities.stream().map(r -> "Rarity." + r).toList()))
                        .build())
                .addField(FieldSpec.builder(String.class, "ALPHABET", Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", ALPHABET)
                        .build())
                .addField(FieldSpec.builder(String.class, "TABLE", Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", table.toString())
                        .build())
                .addMethod(MethodSpec.methodBuilder("index")
                        .addModifiers(Modifier.STATIC)
                        .returns(int.class)
                        .addParameter(int.class, "at")
                        .addStatement("return ALPHABET.indexOf(TABLE.charAt(at))")
                        .build())
                .build());

        enumBuilder.addMethod(MethodSpec.constructorBuilder()
                .addStatement("int at = 3 * ordinal()")
                .addStatement("this.material = Defaults.MATERIALS[Defaults.index(at) * Defaults.ALPHABET.length() + Defaults.index(at + 1)]")
                .addStatement("this.rarity = Defaults.RARITIES[Defaults.index(at + 2)]")
                .build()
        );

        // get(String)
        enumBuilder.addMethod(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(nullable)
                .returns(ClassName.bestGuess("ItemType"))
                .addParameter(String.class, "name")
                .beginControlFlow("try")
                .addStatement(
                        "return ItemType.valueOf(name.replace($S, $S).toUpperCase())",
                        "minecraft:", ""
                )
                .nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("return null")
                .endControlFlow()
                .build()
        );

        enumBuilder.addMethod(MethodSpec.methodBuilder("isVanillaReplaced")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(boolean.class)
                .addParameter(String.class, "item")
                .addStatement("return get(item) != null")
                .build()
        );

        enumBuilder.addMethod(
                MethodSpec.methodBuilder("fromMaterial")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addAnnotation(nullable)
                        .returns(ClassName.bestGuess("ItemType"))
                        .addParameter(material, "material")
                        .addStatement(
                                "String materialName = material.key().value()"
                        )
                        .addStatement(
                                "String formattedItemName = $T.toNormalCase(materialName)",
                                ClassName.get("net.swofty.commons", "StringUtility")
                        )
                        .beginControlFlow("for (ItemType itemType : ItemType.values())")
                        .beginControlFlow(
                                "if (itemType.material == material && formattedItemName.equals($T.toNormalCase(itemType.getDisplayName())))",
                                ClassName.get("net.swofty.commons", "StringUtility")
                        )
                        .addStatement("return itemType")
                        .endControlFlow()
                        .endControlFlow()
                        .addStatement("return null")
                        .build()
        );

        enumBuilder.addMethod(
                MethodSpec.methodBuilder("getDisplayName")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addStatement(
                                "return $T.toNormalCase(this.name())",
                                ClassName.get("net.swofty.commons", "StringUtility")
                        )
                        .build()
        );

        Files.createDirectories(outputDir);
        String source = JavaFile.builder("net.swofty.commons.skyblock.item", enumBuilder.build())
                .addFileComment("AUTO-GENERATED FILE. DO NOT EDIT.")
                .build()
                .toString()
                .replaceAll("(?m)^  ([A-Z0-9_]+)([,;])$", "  $1()$2");
        Path file = outputDir.resolve("net/swofty/commons/skyblock/item/ItemType.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
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
