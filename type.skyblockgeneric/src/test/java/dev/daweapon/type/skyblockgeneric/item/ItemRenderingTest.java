package dev.daweapon.type.skyblockgeneric.item;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.item.components.FishingRodMetadataComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRenderingTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
    }

    @Test
    void everyFishingRodRendersItsLoreAndName() {
        List<String> failures = new ArrayList<>();
        int rods = 0;
        for (ItemType type : ItemType.values()) {
            SkyBlockItem item = new SkyBlockItem(type);
            if (!item.hasComponent(FishingRodMetadataComponent.class)) continue;
            rods++;
            try {
                String name = item.getDisplayName();
                if (name == null || name.isBlank()) failures.add(type + ": blank name");
                if (item.getLoreText().isEmpty()) failures.add(type + ": no lore");
            } catch (StackOverflowError error) {
                failures.add(type + ": StackOverflowError");
            } catch (RuntimeException exception) {
                failures.add(type + ": " + exception);
            }
        }
        assertTrue(rods > 0, "no fishing rods were loaded");
        assertTrue(failures.isEmpty(), "Rods that fail to render: " + String.join(", ", failures));
    }

    @Test
    void starterLavaRodKeepsItsName() {
        SkyBlockItem rod = new SkyBlockItem(ItemType.STARTER_LAVA_ROD);
        assertTrue(rod.getDisplayName().contains("Starter Lava Rod"), rod.getDisplayName());
    }

    @Test
    void everyItemSurvivesAStackRoundTrip() {
        List<String> failures = new ArrayList<>();
        for (ItemType type : ItemType.values()) {
            SkyBlockItem original = new SkyBlockItem(type, 3);
            if (original.isAir()) continue;
            SkyBlockItem loaded = new SkyBlockItem(original.getItemStack());
            for (ItemAttribute attribute : original.attributes) {
                String expected = attribute.saveIntoString();
                String actual = loaded.getAttribute(attribute.getKey()).saveIntoString();
                if (!java.util.Objects.equals(expected, actual) && !attribute.getKey().equals("statistics")) {
                    failures.add(type + "." + attribute.getKey());
                }
            }
            if (loaded.getAmount() != 3) failures.add(type + ".amount");
        }
        assertTrue(failures.isEmpty(), "Attributes lost on a round trip: " + String.join(", ", failures));
    }

    @Test
    void attributeTagsMatchMinestomTags() {
        SkyBlockItem item = new SkyBlockItem(ItemType.STARTER_LAVA_ROD);
        ItemStack stack = item.getItemStack();
        for (ItemAttribute attribute : item.attributes) {
            assertEquals(stack.getTag(Tag.String(attribute.getKey())),
                    ItemAttributeTags.string(ItemAttributeTags.of(stack), attribute.getKey()), attribute.getKey());
        }
        assertTrue(SkyBlockItem.isSkyBlockItem(stack));
        assertFalse(ItemAttributeTags.flag(ItemAttributeTags.of(stack), "uneditable"));
        assertTrue(ItemAttributeTags.flag(ItemAttributeTags.of(stack.withTag(Tag.Boolean("uneditable"), true)), "uneditable"));
    }

    @Test
    void loadingAndRenderingAnItemIsCheap() {
        ItemStack stack = new NonPlayerItemUpdater(new SkyBlockItem(ItemType.STARTER_LAVA_ROD)).getUpdatedItem().build();
        for (int i = 0; i < 2_000; i++) new SkyBlockItem(stack);

        int loads = 20_000;
        long start = System.nanoTime();
        for (int i = 0; i < loads; i++) new SkyBlockItem(stack);
        double loadMicros = (System.nanoTime() - start) / 1_000.0 / loads;

        int renders = 2_000;
        start = System.nanoTime();
        for (int i = 0; i < renders; i++) new NonPlayerItemUpdater(new SkyBlockItem(stack)).getUpdatedItem();
        double renderMicros = (System.nanoTime() - start) / 1_000.0 / renders;

        List<ItemAttribute> attributes = new ArrayList<>(ItemAttribute.getPossibleAttributes());
        int samples = 2_000;
        start = System.nanoTime();
        for (int i = 0; i < samples; i++) {
            for (ItemAttribute attribute : attributes) stack.getTag(Tag.String(attribute.getKey()));
        }
        double perTagReadMicros = (System.nanoTime() - start) / 1_000.0 / samples;
        start = System.nanoTime();
        for (int i = 0; i < samples; i++) {
            var nbt = ItemAttributeTags.of(stack);
            for (ItemAttribute attribute : attributes) ItemAttributeTags.string(nbt, attribute.getKey());
        }
        double compoundReadMicros = (System.nanoTime() - start) / 1_000.0 / samples;
        start = System.nanoTime();
        for (int i = 0; i < samples; i++) {
            ItemStack.Builder builder = ItemStack.builder(stack.material());
            for (ItemAttribute attribute : attributes) builder.set(Tag.String(attribute.getKey()), attribute.saveIntoString());
        }
        double perTagWriteMicros = (System.nanoTime() - start) / 1_000.0 / samples;
        start = System.nanoTime();
        for (int i = 0; i < samples; i++) ItemAttributeTags.write(ItemStack.builder(stack.material()), attributes);
        double compoundWriteMicros = (System.nanoTime() - start) / 1_000.0 / samples;

        System.out.printf("Reading %d attributes: per-tag %.1f us, one compound %.1f us; writing: per-tag %.1f us, one compound %.1f us%n",
                attributes.size(), perTagReadMicros, compoundReadMicros, perTagWriteMicros, compoundWriteMicros);
        System.out.printf("SkyBlockItem(ItemStack): %.1f us/item, full lore render: %.1f us/item%n", loadMicros, renderMicros);
        assertTrue(compoundReadMicros < perTagReadMicros, "compound reads should beat per-tag reads");
        assertTrue(loadMicros < 1_000, "loading one item took " + loadMicros + "us");
    }
}
