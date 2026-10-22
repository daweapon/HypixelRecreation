package dev.daweapon.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryConditions;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffectKind;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.Recombobulation;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
import dev.daweapon.type.skyblockgeneric.item.components.NotFinishedYetComponent;
import dev.daweapon.type.skyblockgeneric.item.events.AccessoryFallDamageEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessoriesBatch3Test {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path BATCH = Path.of("../configuration/skyblock/items/accessories");
    private static final List<String> RIFT_ONLY = List.of("CRUX_TALISMAN_1", "CRUX_TALISMAN_2", "CRUX_TALISMAN_3",
            "CRUX_TALISMAN_4", "CRUX_TALISMAN_5", "CRUX_TALISMAN_6", "CRUX_TALISMAN_7", "DEFECTIVE_MONITOR",
            "GARLIC_FLAVORED_GUMMY_BEAR", "HARMONIOUS_SURGERY_TOOLKIT", "RING_OF_BROKEN_LOVE");

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

    @SuppressWarnings("unchecked")
    static List<String> batch() throws IOException {
        List<String> ids = new ArrayList<>();
        try (Stream<Path> files = Files.list(BATCH)) {
            for (Path file : files.filter(path -> path.getFileName().toString().startsWith("ac3_")).sorted().toList()) {
                for (Object entry : (List<?>) YamlFileUtils.loadYaml(file.toFile()).get("items"))
                    ids.add(String.valueOf(((Map<String, Object>) entry).get("id")));
            }
        }
        return ids;
    }

    private static AccessoryComponent accessory(String id) {
        ItemType type = ItemType.get(id);
        assertNotNull(type, id + " is missing from ItemType; regenerate it");
        SkyBlockItem item = new SkyBlockItem(type);
        assertTrue(item.hasComponent(AccessoryComponent.class), id + ": ACCESSORY block failed to parse");
        return item.getComponent(AccessoryComponent.class);
    }

    private static AccessoryEffect first(String id, AccessoryEffectKind kind) {
        return accessory(id).getEffects().stream().filter(effect -> effect.kind() == kind).findFirst()
                .orElseThrow(() -> new AssertionError(id + " has no " + kind));
    }

    @Test
    void everyBatchIdIsAnAccessoryWithParsedEffects() throws IOException {
        List<String> ids = batch();
        assertEquals(61, ids.size());
        assertEquals(61, new HashSet<>(ids).size());
        List<String> failures = new ArrayList<>();
        for (String id : ids) {
            AccessoryComponent component = accessory(id);
            for (AccessoryEffect effect : component.getEffects()) {
                if (effect.kind() == AccessoryEffectKind.HANDLER && !AccessoryHandlers.isKnown(effect.handler()))
                    failures.add(id + ": unknown handler " + effect.handler());
                if ((effect.kind() == AccessoryEffectKind.TEXT || effect.kind() == AccessoryEffectKind.INACTIVE)
                        && effect.text().isEmpty())
                    failures.add(id + ": text effect without text");
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void familiesResolve() {
        AccessoryFamilies families = AccessoryFamilies.get();
        assertEquals("SPEED_RELIC", families.byId(families.familyOf("SPEED_TALISMAN").id()).best());
        assertEquals("SPIKED_ATROCITY", families.byId(families.familyOf("BAIT_RING").id()).best());
        assertEquals("MOONLIGHT_CRYSTAL", families.byId(families.familyOf("NIGHT_CRYSTAL").id()).best());
        assertEquals("FRESHLY_BAKED_HEIRLOOM", families.byId(families.familyOf("FRESHLY_BAKED_TALISMAN").id()).best());
        assertEquals("POWDER_OF_THE_PEOPLE", families.byId(families.familyOf("POWDER_OF_ONE").id()).best());
        assertEquals("TWO_IQ_POINT", families.byId(families.familyOf("IQ_POINT").id()).best());
        assertEquals(families.familyOf("DANTE_RING").id(), families.familyOf("DANTE_TALISMAN").id());
        assertNull(families.familyOf("GRAVITY_TALISMAN"));
        assertNull(families.familyOf("MAGIC_8_BALL"));
    }

    @Test
    void riftOnlyItemsGiveNoPowerAndCannotBeRecombobulated() {
        for (String id : RIFT_ONLY) {
            assertEquals(AccessoryRealm.RIFT, accessory(id).getRealm(), id);
            assertEquals(0, AccessoryPower.itemPower(new SkyBlockItem(ItemType.get(id)), 0, false), id);
            assertFalse(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.get(id))), id);
        }
        assertEquals(Rarity.DIVINE, new SkyBlockItem(ItemType.CRUX_TALISMAN_7).getAttributeHandler().getRarity());
        assertEquals(AccessoryRealm.BINGO, accessory("BINGO_RIFT_TALISMAN").getRealm());
    }

    @Test
    void flagsFollowHypixel() {
        assertTrue(accessory("INFINIPOT").isDungeon());
        assertFalse(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.PANDORAS_BOX)));
        assertFalse(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.SAFETY_BADGE)));
        assertTrue(Recombobulation.canRecombobulate(new SkyBlockItem(ItemType.SPEED_RELIC)));
        assertEquals(Rarity.MYTHIC, accessory("PANDORAS_BOX").getDynamicMaxRarity());
        assertEquals(Rarity.LEGENDARY, accessory("PULSE_RING").getDynamicMaxRarity());
    }

    @Test
    void batchStatsMatchHypixel() {
        ItemStatistics relic = new SkyBlockItem(ItemType.SPEED_RELIC).getAttributeHandler().getStatistics();
        assertEquals(7, relic.getOverall(ItemStatistic.SPEED), 0.0001);
        assertEquals(1, relic.getOverall(ItemStatistic.BONUS_ATTACK_SPEED), 0.0001);
        assertEquals(1, relic.getOverall(ItemStatistic.MINING_SPEED), 0.0001);
        assertEquals(5, new SkyBlockItem(ItemType.FRESHLY_BAKED_HEIRLOOM).getAttributeHandler().getStatistics()
                .getOverall(ItemStatistic.OVERBLOOM), 0.0001);
        assertEquals(15, new SkyBlockItem(ItemType.DWARVEN_METAL).getAttributeHandler().getStatistics()
                .getOverall(ItemStatistic.DWARVEN_METAL_FORTUNE), 0.0001);
        AccessoryEffect night = first("MOONLIGHT_CRYSTAL", AccessoryEffectKind.STAT);
        assertEquals(AccessoryConditions.TimeOfDay.NIGHT, night.conditions().time());
        assertEquals(10, night.statistics().getOverall(ItemStatistic.STRENGTH), 0.0001);
    }

    @Test
    void handlerNumbersComeFromYaml() {
        assertEquals(5, first("BAIT_RING", AccessoryEffectKind.HANDLER).percent(), 0.0001);
        assertEquals(10, first("SPIKED_ATROCITY", AccessoryEffectKind.HANDLER).percent(), 0.0001);
        assertEquals(2, first("TWO_IQ_POINT", AccessoryEffectKind.HANDLER).percent(), 0.0001);
        assertEquals(10, first("POWDER_OF_THE_PEOPLE", AccessoryEffectKind.HANDLER).percent(), 0.0001);
        assertEquals("GRAVITY_TALISMAN", first("GRAVITY_TALISMAN", AccessoryEffectKind.HANDLER).handler());
        assertTrue(AccessoryHandlerStats.isStatHandler("MAGIC_8_BALL"));
    }

    @Test
    void gravityAndMagic8BallFormulas() {
        assertEquals(10, AccessoryHandlerStats.gravityBonus(0), 0.0001);
        assertEquals(9, AccessoryHandlerStats.gravityBonus(15), 0.0001);
        assertEquals(1, AccessoryHandlerStats.gravityBonus(500), 0.0001);
        Set<Integer> seen = new HashSet<>();
        for (int season = 0; season < 400; season++) {
            int index = AccessoryHandlerStats.magic8BallIndex("player", season / 4, season % 4);
            assertEquals(index, AccessoryHandlerStats.magic8BallIndex("player", season / 4, season % 4));
            seen.add(index);
        }
        assertEquals(5, seen.size());
        assertEquals(25, AccessoryHandlerStats.magic8BallBonus(0).getOverall(ItemStatistic.FARMING_FORTUNE), 0.0001);
        assertEquals(25, AccessoryHandlerStats.magic8BallBonus(4).getOverall(ItemStatistic.STRENGTH), 0.0001);
    }

    @Test
    void auditedAccessoriesAreCorrected() {
        AccessoryEffect zombie = first("ZOMBIE_ARTIFACT", AccessoryEffectKind.DAMAGE_REDUCTION);
        assertEquals(15, zombie.percent(), 0.0001);
        assertEquals(Set.of(MobType.UNDEAD), zombie.mobTypes());
        assertEquals(Set.of(MobType.ARTHROPOD), first("SPIDER_TALISMAN", AccessoryEffectKind.DAMAGE_REDUCTION).mobTypes());
        assertEquals(25, first("ENDER_RELIC", AccessoryEffectKind.DAMAGE_REDUCTION).percent(), 0.0001);
        assertEquals(15, first("GLACIAL_ARTIFACT", AccessoryEffectKind.STAT).statistics()
                .getOverall(ItemStatistic.MINING_WISDOM), 0.0001);
        assertEquals(3, first("INFERNAL_KUUDRA_CORE", AccessoryEffectKind.STAT).statistics()
                .getOverall(ItemStatistic.HUNTER_FORTUNE), 0.0001);
        assertEquals(30, first("CAMPFIRE_TALISMAN_29", AccessoryEffectKind.HANDLER).statistics()
                .getOverall(ItemStatistic.HEALTH_REGENERATION), 0.0001);
        assertEquals(18, first("SOUL_CAMPFIRE_TALISMAN_8", AccessoryEffectKind.HANDLER).statistics()
                .getOverall(ItemStatistic.DEFENSE), 0.0001);
        assertEquals(0.3, new SkyBlockItem(ItemType.BEASTMASTER_CREST_LEGENDARY).getAttributeHandler().getStatistics()
                .getOverall(ItemStatistic.HEALTH), 0.0001);
        assertEquals(AccessoryEffectKind.INACTIVE, accessory("MASTER_SKULL_TIER_7").getEffects().getFirst().kind());
        for (String id : List.of("ZOMBIE_ARTIFACT", "COIN_TALISMAN", "CAMPFIRE_TALISMAN_2", "TITANIUM_RELIC", "POWER_RELIC"))
            assertFalse(new SkyBlockItem(ItemType.get(id)).hasComponent(NotFinishedYetComponent.class), id);
    }

    @Test
    void featherReducesFallDamage() {
        assertEquals(6, AccessoryFallDamageEvent.reduce(16, 5, 0), 0.0001);
        assertEquals(0, AccessoryFallDamageEvent.reduce(16, 10, 15), 0.0001);
        assertEquals(17, AccessoryFallDamageEvent.reduce(40, 10, 15), 0.0001);
    }
}
