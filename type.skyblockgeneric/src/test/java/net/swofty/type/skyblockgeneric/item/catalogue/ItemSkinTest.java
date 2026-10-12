package net.swofty.type.skyblockgeneric.item.catalogue;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ItemSkinComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemSkinTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path COSMETICS = ITEMS.resolve("catalogue/cosmetics_a.yml");
    private static final List<String> SKIN_IDS = new ArrayList<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    ItemConfigParser.parseItem(item);
                    if (file.normalize().equals(COSMETICS.normalize()) && hasSkinComponent(item)) SKIN_IDS.add(String.valueOf(item.get("id")));
                }
            }
        }
    }

    private static boolean hasSkinComponent(Map<String, Object> item) {
        return item.get("components") instanceof List<?> components
                && components.stream().anyMatch(c -> c instanceof Map<?, ?> m && "ITEM_SKIN".equals(m.get("id")));
    }

    private static SkyBlockItem item(String id) {
        return new SkyBlockItem(ItemType.get(id));
    }

    private static boolean canApply(String skin, SkyBlockItem target) {
        return item(skin).getComponent(ItemSkinComponent.class).canApplyTo(target);
    }

    @Test
    void everySkinParsesWithTextureAndTargets() {
        assertTrue(SKIN_IDS.size() >= 50, SKIN_IDS.size() + " skins");
        for (String id : SKIN_IDS) {
            ItemSkinComponent skin = item(id).getComponent(ItemSkinComponent.class);
            assertNotNull(skin, id);
            assertTrue(skin.getTexture().matches("[0-9a-f]{40,70}"), id);
            assertFalse(skin.getTargets().isEmpty(), id);
            assertNotNull(AnvilCombineRegistry.getHandler("ITEM_SKIN:" + id), id);
        }
    }

    @Test
    void skinAppliesOnlyToItsTargets() {
        assertTrue(canApply("MERCENARY_KNIGHT", item("MERCENARY_HELMET")));
        assertFalse(canApply("MERCENARY_KNIGHT", item("STARLIGHT_HELMET")));
        assertTrue(canApply("BACKPACK_COOLER", item("SMALL_BACKPACK")));
        assertTrue(canApply("BACKPACK_COOLER", item("JUMBO_BACKPACK")));
        assertFalse(canApply("BACKPACK_COOLER", item("MERCENARY_HELMET")));
        assertTrue(canApply("PUMPKIN_FLUX", item("RADIANT_POWER_ORB")));
    }

    @Test
    void animatedSkinsAreRefused() {
        assertTrue(item("HOLY_DRAGON_DRAKE").getComponent(ItemSkinComponent.class).isAnimated());
        assertFalse(canApply("HOLY_DRAGON_DRAKE", item("HOLY_DRAGON_HELMET")));
    }

    @Test
    void appliedSkinChangesTextureAndPersists() {
        SkyBlockItem helmet = item("MERCENARY_HELMET");
        assertNull(ItemSkinComponent.textureOf(helmet));

        var handler = AnvilCombineRegistry.getHandler("ITEM_SKIN:MERCENARY_KNIGHT");
        handler.combineFunction().apply(helmet, item("MERCENARY_KNIGHT"));

        String texture = item("MERCENARY_KNIGHT").getComponent(ItemSkinComponent.class).getTexture();
        assertEquals(ItemType.get("MERCENARY_KNIGHT"), helmet.getAttributeHandler().getItemSkin());
        assertEquals(texture, ItemSkinComponent.textureOf(helmet));
        assertFalse(canApply("MERCENARY_KNIGHT", helmet));

        SkyBlockItem saved = new SkyBlockItem(new UnderstandableSkyBlockItemSerializer()
                .deserialize(new UnderstandableSkyBlockItemSerializer().serialize(helmet.toUnderstandable())));
        assertEquals(texture, ItemSkinComponent.textureOf(saved));
    }

    @Test
    void unskinnedItemsHaveNoSkinAttribute() {
        assertNull(item("MERCENARY_HELMET").getAttributeHandler().getItemSkin());
    }
}
