package dev.daweapon.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffectKind;
import dev.daweapon.type.skyblockgeneric.accessory.hatcessory.Hatcessories;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.Recombobulation;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
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

class AccessoriesBatch1Test {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final List<String> COLOURS = List.of("AQUA", "BLACK", "GREEN", "LIME", "ORANGE", "PINK", "PURPLE", "RED", "YELLOW");
    private static final List<String> SLOTHS = List.of("CHEEKY", "COOL", "CUTE", "DERP", "FLUSHED", "GRUMPY", "HAPPY",
            "REGULAR", "SHOCK", "TEARS");
    private static final List<String> RIFT_ONLY = List.of("PUNCHCARD_ARTIFACT", "RING_OF_ETERNAL_LOVE", "SATELITE", "WARDING_TRINKET");

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

    static List<String> hatcessories() {
        List<String> ids = new ArrayList<>();
        for (String colour : COLOURS) {
            ids.add("BALLOON_HAT_2024_" + colour);
            ids.add("BALLOON_HAT_2025_" + colour);
            ids.add("CAKE_HAT_2026_" + colour);
            ids.add("PARTY_HAT_CRAB_" + colour);
            ids.add("PARTY_HAT_CRAB_" + colour + "_ANIMATED");
        }
        for (String sloth : SLOTHS) ids.add("PARTY_HAT_SLOTH_" + sloth);
        return ids;
    }

    static List<String> batch() {
        List<String> ids = new ArrayList<>(hatcessories());
        ids.addAll(RIFT_ONLY);
        ids.addAll(List.of("SUNSHINE_CRYSTAL", "TINY_DANCER"));
        return ids;
    }

    private static AccessoryComponent accessory(String id) {
        ItemType type = ItemType.get(id);
        assertNotNull(type, id + " is missing from ItemType; regenerate it");
        SkyBlockItem item = new SkyBlockItem(type);
        assertTrue(item.hasComponent(AccessoryComponent.class), id + ": ACCESSORY block failed to parse");
        return item.getComponent(AccessoryComponent.class);
    }

    @Test
    void everyBatchIdIsAnAccessoryWithParsedEffects() {
        assertEquals(61, batch().size());
        List<String> failures = new ArrayList<>();
        for (String id : batch()) {
            AccessoryComponent component = accessory(id);
            for (AccessoryEffect effect : component.getEffects()) {
                if (effect.kind() == AccessoryEffectKind.HANDLER && !AccessoryHandlers.isKnown(effect.handler()))
                    failures.add(id + ": unknown handler " + effect.handler());
                if (effect.kind() != AccessoryEffectKind.STAT && effect.text().isEmpty())
                    failures.add(id + ": effect without text");
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void familiesResolve() {
        AccessoryFamilies families = AccessoryFamilies.get();
        for (String id : hatcessories()) {
            assertEquals(Hatcessories.FAMILY_ID, families.familyOf(id).id(), id);
            assertTrue(Hatcessories.isHatcessory(ItemType.get(id)), id);
        }
        assertEquals("CAKE_HAT_2026_RED", families.byId(Hatcessories.FAMILY_ID).best());
        assertEquals("DAY_CRYSTAL", families.familyOf("SUNSHINE_CRYSTAL").id());
        assertEquals("SUNSHINE_CRYSTAL", families.byId("DAY_CRYSTAL").best());
        for (String id : RIFT_ONLY) assertNull(families.familyOf(id), id);
        assertNull(families.familyOf("TINY_DANCER"));
        assertFalse(Hatcessories.isHatcessory(ItemType.TINY_DANCER));
    }

    @Test
    void onlyOneHatcessoryCounts() {
        List<OwnedAccessory> hats = new ArrayList<>();
        for (String id : hatcessories()) hats.add(OwnedAccessory.of(new SkyBlockItem(ItemType.get(id)), true));
        List<OwnedAccessory> active = AccessoryOwnership.dedupe(hats);
        assertEquals(1, active.size());
        assertEquals(1, AccessoryStats.of(null, active, ignored -> dev.daweapon.commons.skyblock.statistics.ItemStatistics.empty())
                .getOverall(ItemStatistic.MAGIC_FIND), 0.0001);
        assertEquals(Rarity.SPECIAL.getAccessoryPower(), AccessoryPower.of(active, 0, false, false));
    }

    @Test
    void riftOnlyItemsStayInTheRift() {
        for (String id : RIFT_ONLY) {
            AccessoryComponent component = accessory(id);
            assertEquals(AccessoryRealm.RIFT, component.getRealm(), id);
            assertEquals(0, AccessoryPower.itemPower(new SkyBlockItem(ItemType.get(id)), 0, false), id);
        }
        assertEquals(AccessoryRealm.ANYWHERE, accessory("TINY_DANCER").getRealm());
    }

    @Test
    void recombobulationFollowsHypixel() {
        assertTrue(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.CAKE_HAT_2026_RED)));
        assertTrue(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.SUNSHINE_CRYSTAL)));
        assertTrue(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.TINY_DANCER)));
        for (String id : RIFT_ONLY) assertFalse(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.get(id))), id);
    }

    @Test
    void sunshineCrystalIsADaytimeStat() {
        AccessoryEffect effect = accessory("SUNSHINE_CRYSTAL").getEffects().getFirst();
        assertEquals(AccessoryEffectKind.STAT, effect.kind());
        assertEquals(10, effect.statistics().getOverall(ItemStatistic.STRENGTH), 0.0001);
        assertEquals(10, effect.statistics().getOverall(ItemStatistic.DEFENSE), 0.0001);
    }

    @Test
    void partyTimeCountsWholeSkyBlockYears() {
        long year = Hatcessories.SKYBLOCK_YEAR_MILLIS;
        assertEquals(0, Hatcessories.yearsSince(0, year * 5));
        assertEquals(0, Hatcessories.yearsSince(1000, 1000 + year - 1));
        assertEquals(1, Hatcessories.yearsSince(1000, 1000 + year));
        assertEquals(7, Hatcessories.yearsSince(1000, 1000 + year * 7 + 5));
    }
}
