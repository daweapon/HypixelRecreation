package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FurnaceStateTest {
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

    private static ItemStack stack(String id, int amount) {
        return new SkyBlockItem(ItemType.get(id), amount).getItemStack();
    }

    @Test
    void oneCoalCooksEightItems() {
        FurnaceState furnace = new FurnaceState();
        furnace.input = stack("IRON_ORE", 10);
        furnace.fuel = stack("COAL", 1);
        furnace.advance(200 * 8);
        assertEquals(2, furnace.input.amount());
        assertEquals(8, furnace.output.amount());
        assertTrue(furnace.fuel.isAir());
    }

    @Test
    void nothingCooksWithoutFuel() {
        FurnaceState furnace = new FurnaceState();
        furnace.input = stack("IRON_ORE", 4);
        furnace.advance(10_000);
        assertEquals(4, furnace.input.amount());
        assertTrue(furnace.output.isAir());
    }

    @Test
    void progressSurvivesASaveAndReload() {
        FurnaceState furnace = new FurnaceState();
        furnace.input = stack("IRON_ORE", 2);
        furnace.fuel = stack("COAL", 1);
        furnace.advance(100);
        FurnaceState reloaded = FurnaceState.parse(furnace.serialize());
        assertEquals(100, reloaded.cookProgress);
        assertEquals(1500, reloaded.burnLeft);
        reloaded.advance(100);
        assertEquals(1, reloaded.output.amount());
        assertFalse(reloaded.isEmpty());
    }

    @Test
    void catchesUpOnTimePassedWhileClosed() {
        FurnaceState furnace = new FurnaceState();
        furnace.input = stack("IRON_ORE", 3);
        furnace.fuel = stack("COAL", 1);
        long start = furnace.touchedAt;
        furnace.advanceTo(start + 200 * 50 * 2);
        assertEquals(2, furnace.output.amount());
    }

    @Test
    void anEmptyFurnaceIsEmptyAndAChestRoundTripsItsPage() {
        assertTrue(new FurnaceState().isEmpty());
        String raw = FurnitureContents.withPage(null, 3, 1, java.util.Collections.nCopies(FurnitureContents.PAGE_SLOTS, ItemStack.AIR));
        assertTrue(FurnitureContents.isEmpty(raw));
        java.util.List<ItemStack> page = new java.util.ArrayList<>(java.util.Collections.nCopies(FurnitureContents.PAGE_SLOTS, ItemStack.AIR));
        page.set(4, stack("COAL", 12));
        String filled = FurnitureContents.withPage(raw, 3, 2, page);
        assertFalse(FurnitureContents.isEmpty(filled));
        assertEquals(12, FurnitureContents.page(filled, 2).get(4).amount());
        assertTrue(FurnitureContents.page(filled, 0).get(4).isAir());
    }
}
