package net.swofty.type.skyblockgeneric.accessory.power;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.accessory.enrichment.Enrichment;
import net.swofty.type.skyblockgeneric.accessory.enrichment.Enrichments;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PowerStoneComponent;
import net.swofty.type.skyblockgeneric.staff.MaxPowers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowersTest {
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
    void multiplierFollowsTheWikiFormula() {
        assertEquals(0, PowerScaling.multiplier(0));
        assertEquals(9.642, PowerScaling.multiplier(250), 0.001);
        assertEquals(29.97 * Math.pow(Math.log(0.0019 * 1000 + 1), 1.2), PowerScaling.multiplier(1000), 1e-9);
        assertEquals(32.312, PowerScaling.multiplier(1000), 0.001);
        assertEquals(52.972, PowerScaling.multiplier(2100), 0.001);
        assertTrue(PowerScaling.multiplier(500) > PowerScaling.multiplier(250));
    }

    @Test
    void scaledStatsMatchHypixelTooltipsAt250AccessoryPower() {
        ItemStatistics itchy = PowerScaling.scaled(AccessoryPowerType.ITCHY, 250);
        assertEquals("69.42", StatText.number(itchy.getOverall(ItemStatistic.STRENGTH)));
        assertEquals("80.99", StatText.number(itchy.getOverall(ItemStatistic.CRITICAL_DAMAGE)));
        assertEquals("20.83", StatText.number(itchy.getOverall(ItemStatistic.BONUS_ATTACK_SPEED)));
        assertEquals("5.79", StatText.number(itchy.getOverall(ItemStatistic.SPEED)));
        assertEquals("115.7", StatText.number(PowerScaling.scaled(AccessoryPowerType.STRONG, 250).getOverall(ItemStatistic.STRENGTH)));
        assertEquals("185.13", StatText.number(PowerScaling.scaled(AccessoryPowerType.HURTFUL, 250).getOverall(ItemStatistic.CRITICAL_DAMAGE)));

        ItemStatistics withBonus = PowerScaling.statistics(AccessoryPowerType.ITCHY, 1000);
        assertEquals(7.2 * PowerScaling.multiplier(1000) + 15, withBonus.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(15, PowerScaling.statistics(AccessoryPowerType.ITCHY, 0).getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(3.35 * PowerScaling.multiplier(750), PowerScaling.scaled(AccessoryPowerType.FORTUITOUS, 750)
                .getOverall(ItemStatistic.HEALTH), 1e-9);
    }

    @Test
    void thereAreTenDefaultAndTwentyTwoStonePowers() {
        assertEquals(10, AccessoryPowerType.values().length - AccessoryPowerType.stonePowers().size());
        assertEquals(22, AccessoryPowerType.stonePowers().size());
        for (AccessoryPowerType power : AccessoryPowerType.stonePowers()) {
            ItemType stone = power.stone();
            assertNotNull(stone, power + "'s stone " + power.getStoneId() + " is missing from ItemType");
            assertEquals(power, AccessoryPowerType.byStone(stone));
            assertTrue(new SkyBlockItem(stone).hasComponent(PowerStoneComponent.class), stone + " has no POWER_STONE");
            assertFalse(PowerStones.lore(new SkyBlockItem(stone)).isEmpty());
            assertNotNull(power.getSource());
        }
    }

    @Test
    void learningAndSelectingFollowTheRules() {
        PowerData data = new PowerData();
        assertTrue(AccessoryPowers.canSelect(data, AccessoryPowerType.WARRIOR, 0));
        assertFalse(AccessoryPowers.canSelect(data, AccessoryPowerType.COMMANDO, 14));
        assertTrue(AccessoryPowers.canSelect(data, AccessoryPowerType.COMMANDO, 15));
        assertFalse(AccessoryPowers.canSelect(data, AccessoryPowerType.SLENDER, 60));

        assertFalse(AccessoryPowers.learn(data, AccessoryPowerType.WARRIOR));
        assertTrue(AccessoryPowers.learn(data, AccessoryPowerType.SLENDER));
        assertFalse(AccessoryPowers.learn(data, AccessoryPowerType.SLENDER));
        assertFalse(AccessoryPowers.canSelect(data, AccessoryPowerType.SLENDER, 29));
        assertTrue(AccessoryPowers.canSelect(data, AccessoryPowerType.SLENDER, 30));

        AccessoryPowers.select(data, AccessoryPowerType.SLENDER);
        ItemStatistics stats = AccessoryPowers.powerStatistics(data, 500);
        assertEquals(6 * PowerScaling.multiplier(500) + 50, stats.getOverall(ItemStatistic.STRENGTH), 1e-9);
        AccessoryPowers.select(data, null);
        assertTrue(data.isPowerChosen());
        assertEquals(0, AccessoryPowers.powerStatistics(data, 500).getOverall(ItemStatistic.STRENGTH));
    }

    @Test
    void learningFromStonesNeedsNineIdenticalStones() {
        PowerData data = new PowerData();
        SkyBlockItem five = new SkyBlockItem(ItemType.FURBALL, 5);
        SkyBlockItem four = new SkyBlockItem(ItemType.FURBALL, 4);
        SkyBlockItem urchin = new SkyBlockItem(ItemType.MAGMA_URCHIN, 9);
        List<SkyBlockItem> grid = new ArrayList<>(Collections.nCopies(9, null));
        grid.set(0, five);
        assertEquals(PowerStones.LearnResult.MISSING, PowerStones.check(data, grid, 60));
        grid.set(4, four);
        assertEquals(PowerStones.LearnResult.LEARNED, PowerStones.check(data, grid, 60));
        assertEquals(PowerStones.LearnResult.COMBAT_TOO_LOW, PowerStones.check(data, grid, 19));
        grid.set(8, urchin);
        assertEquals(PowerStones.LearnResult.MIXED, PowerStones.check(data, grid, 60));
        grid.set(8, new SkyBlockItem(ItemType.SPEED_TALISMAN));
        assertEquals(PowerStones.LearnResult.NOT_A_STONE, PowerStones.check(data, grid, 60));
        grid.set(8, null);
        AccessoryPowers.learn(data, AccessoryPowerType.ITCHY);
        assertEquals(PowerStones.LearnResult.ALREADY_KNOWN, PowerStones.check(data, grid, 60));
    }

    @Test
    void tuningPointsAreOnePerTenAccessoryPower() {
        assertEquals(0, PowerScaling.tuningPoints(9));
        assertEquals(42, PowerScaling.tuningPoints(425));
        assertEquals(210, PowerScaling.tuningPoints(2100));

        PowerData data = new PowerData();
        assertEquals(42, AccessoryPowers.addPoints(data, TuningStat.HEALTH, 50, 425));
        assertEquals(0, AccessoryPowers.addPoints(data, TuningStat.STRENGTH, 1, 425));
        assertEquals(0, AccessoryPowers.unassignedPoints(data, 425));
        assertEquals(10, AccessoryPowers.removePoints(data, TuningStat.HEALTH, 10));
        assertEquals(10, AccessoryPowers.addPoints(data, TuningStat.ATTACK_SPEED, 10, 425));

        ItemStatistics stats = AccessoryPowers.tuningStatistics(data, 425);
        assertEquals(32 * 5, stats.getOverall(ItemStatistic.HEALTH), 1e-9);
        assertEquals(10 * 0.3, stats.getOverall(ItemStatistic.BONUS_ATTACK_SPEED), 1e-9);

        Map<TuningStat, Integer> dropped = AccessoryPowers.effectiveTuning(data, 200);
        assertEquals(20, dropped.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(20, dropped.get(TuningStat.HEALTH));

        AccessoryPowers.saveTemplate(data, 0);
        AccessoryPowers.removePoints(data, TuningStat.HEALTH, 100);
        assertTrue(AccessoryPowers.loadTemplate(data, 0));
        assertEquals(32, data.pointsIn(TuningStat.HEALTH));
        assertFalse(AccessoryPowers.loadTemplate(data, 3));
        assertTrue(AccessoryPowers.isTemplateUnlocked(data, 0));
        assertFalse(AccessoryPowers.isTemplateUnlocked(data, 1));
    }

    @Test
    void enrichmentsApplyToLegendaryAndAboveAndSwap() {
        SkyBlockItem common = new SkyBlockItem(ItemType.SPEED_TALISMAN);
        SkyBlockItem legendary = new SkyBlockItem(ItemType.HEGEMONY_ARTIFACT);
        assertFalse(Enrichments.canEnrich(common));
        assertTrue(Enrichments.canEnrich(legendary));
        assertFalse(Enrichments.canEnrich(new SkyBlockItem(ItemType.STRENGTH_ENRICHMENT)));

        SkyBlockItem epic = new SkyBlockItem(ItemType.WITHER_ARTIFACT);
        assertEquals(Rarity.EPIC, epic.getAttributeHandler().getEffectiveRarity());
        assertFalse(Enrichments.canEnrich(epic));
        epic.getAttributeHandler().setRecombobulated(true);
        assertTrue(Enrichments.canEnrich(epic));

        assertEquals(Enrichment.STRENGTH, Enrichment.byItem(ItemType.STRENGTH_ENRICHMENT));
        assertTrue(Enrichments.canApply(legendary, Enrichment.STRENGTH));
        Enrichments.set(legendary, Enrichment.STRENGTH);
        assertEquals(Enrichment.STRENGTH, Enrichments.of(legendary));
        assertFalse(Enrichments.canApply(legendary, Enrichment.STRENGTH));
        assertTrue(Enrichments.canApply(legendary, Enrichment.HEALTH));
        assertEquals(1, Enrichments.statistics(legendary).getOverall(ItemStatistic.STRENGTH), 1e-9);

        SkyBlockItem reloaded = new SkyBlockItem(legendary.toUnderstandable());
        assertEquals(Enrichment.STRENGTH, Enrichments.of(reloaded));

        DatapointAccessoryBag.PlayerAccessoryBag bag = new DatapointAccessoryBag.PlayerAccessoryBag();
        bag.setInSlot(0, legendary);
        bag.setInSlot(1, common);
        assertEquals(1, Enrichments.swapAll(bag, Enrichment.HEALTH));
        assertEquals(Enrichment.HEALTH, Enrichments.of(legendary));
        assertNull(Enrichments.of(common));
        assertEquals(3, Enrichments.statistics(legendary).getOverall(ItemStatistic.HEALTH), 1e-9);
        assertEquals(0, Enrichments.swapAll(bag, Enrichment.HEALTH));
        assertEquals(11, Enrichment.values().length);
        for (Enrichment enrichment : Enrichment.values()) assertNotNull(enrichment.item(), enrichment.getItemId());
    }

    @Test
    void maxProfileLearnsEverythingPicksTheBestPowerAndIsSafeToRerun() {
        PowerData data = new PowerData();
        MaxPowers.apply(data, 2100, 60);
        assertEquals(22, data.getLearned().size());
        assertEquals(AccessoryPowerType.SLENDER, data.getSelected());
        assertEquals(210, data.pointsIn(MaxPowers.DEFAULT_TUNING));
        assertEquals(AccessoryPowers.TEMPLATE_SLOTS, data.getTemplatesUnlocked());
        String first = DatapointAccessoryPowers.serializer.serialize(data);
        MaxPowers.apply(data, 2100, 60);
        assertEquals(first, DatapointAccessoryPowers.serializer.serialize(data));

        PowerData custom = new PowerData();
        custom.getTuning().put(TuningStat.INTELLIGENCE, 5);
        MaxPowers.apply(custom, 2100, 60);
        assertEquals(5, custom.pointsIn(TuningStat.INTELLIGENCE));
        assertEquals(0, custom.pointsIn(TuningStat.STRENGTH));

        PowerData overBudget = new PowerData();
        overBudget.getTuning().put(TuningStat.HEALTH, 500);
        MaxPowers.apply(overBudget, 2100, 60);
        assertEquals(0, overBudget.pointsIn(TuningStat.HEALTH));
        assertEquals(210, overBudget.pointsIn(TuningStat.STRENGTH));
    }

    @Test
    void savesRoundTripAndOldSavesLoad() {
        PowerData data = new PowerData();
        AccessoryPowers.learn(data, AccessoryPowerType.HURTFUL);
        AccessoryPowers.select(data, AccessoryPowerType.HURTFUL);
        data.getTuning().put(TuningStat.CRIT_CHANCE, 7);
        AccessoryPowers.saveTemplate(data, 0);
        data.setTemplatesUnlocked(2);
        PowerData loaded = DatapointAccessoryPowers.serializer.deserialize(DatapointAccessoryPowers.serializer.serialize(data));
        assertEquals(data.getLearned(), loaded.getLearned());
        assertEquals(AccessoryPowerType.HURTFUL, loaded.getSelected());
        assertTrue(loaded.isPowerChosen());
        assertEquals(7, loaded.pointsIn(TuningStat.CRIT_CHANCE));
        assertEquals(7, loaded.getTemplates().get(0).get(TuningStat.CRIT_CHANCE));
        assertEquals(2, loaded.getTemplatesUnlocked());

        PowerData noPower = new PowerData();
        AccessoryPowers.select(noPower, null);
        PowerData loadedNoPower = DatapointAccessoryPowers.serializer.deserialize(DatapointAccessoryPowers.serializer.serialize(noPower));
        assertTrue(loadedNoPower.isPowerChosen());
        assertNull(loadedNoPower.getSelected());

        for (String old : new String[]{null, "", "{}", "not json", "{\"learned\":[\"REMOVED\",\"STRONG\"],\"selected\":\"GONE\","
                + "\"tuning\":{\"LUCK\":3,\"HEALTH\":-1,\"DEFENSE\":4},\"templates\":{\"x\":{},\"1\":{\"SPEED\":2}}}"}) {
            PowerData parsed = DatapointAccessoryPowers.serializer.deserialize(old);
            assertNotNull(parsed);
            assertEquals(1, parsed.getTemplatesUnlocked());
        }
        PowerData legacy = DatapointAccessoryPowers.serializer.deserialize("{\"learned\":[\"REMOVED\",\"STRONG\"],\"selected\":\"GONE\","
                + "\"tuning\":{\"LUCK\":3,\"HEALTH\":-1,\"DEFENSE\":4},\"templates\":{\"x\":{},\"1\":{\"SPEED\":2}}}");
        assertEquals(1, legacy.getLearned().size());
        assertNull(legacy.getSelected());
        assertEquals(4, legacy.assignedPoints());
        assertEquals(2, legacy.getTemplates().get(1).get(TuningStat.SPEED));
        assertFalse(DatapointAccessoryPowers.serializer.deserialize("{}").isPowerChosen());
    }
}
