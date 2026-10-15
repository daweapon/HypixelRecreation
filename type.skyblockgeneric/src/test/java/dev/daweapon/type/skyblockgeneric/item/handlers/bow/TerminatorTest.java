package dev.daweapon.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.components.AbilityComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ShortBowComponent;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminatorTest {
    private static ConfigurableSkyBlockItem terminator;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils
                .loadYaml(Path.of("../configuration/skyblock/items/bows.yml").toFile()).get("items");
        terminator = ItemConfigParser.parseItem(items.stream().filter(item -> "TERMINATOR".equals(item.get("id"))).findFirst().orElseThrow());
    }

    @Test
    void terminatorCarriesTheWikiStats() {
        ItemStatistics stats = terminator.getDefaultStatistics();
        assertEquals(310, stats.getOverall(ItemStatistic.DAMAGE));
        assertEquals(50, stats.getOverall(ItemStatistic.STRENGTH));
        assertEquals(250, stats.getOverall(ItemStatistic.CRITICAL_DAMAGE));
        assertEquals(40, stats.getOverall(ItemStatistic.BONUS_ATTACK_SPEED));
    }

    @Test
    void terminatorIsAThreeArrowShortbowWithSalvation() {
        ShortBowComponent bow = terminator.getComponent(ShortBowComponent.class);
        assertEquals(0.5f, bow.getCooldown());
        assertEquals(3, bow.getArrows());
        assertEquals(5, bow.getSpread());
        assertEquals(4, bow.getCritChanceDivisor());
        assertFalse(bow.isShouldBeArrow());
        assertTrue(terminator.getComponent(AbilityComponent.class).getAbilityIds().contains(Salvation.ABILITY_ID));
    }

    @Test
    void aVolleyFansOutAroundTheCrosshair() {
        assertEquals(List.of(0.0, -5.0, 5.0), ShortBowVolley.yawOffsets(3, 5));
        assertEquals(List.of(0.0), ShortBowVolley.yawOffsets(1, 5));
        assertEquals(List.of(-2.5, 2.5), ShortBowVolley.hydraOffsets(2));
    }

    @Test
    void shotCooldownFollowsTheAttackSpeedTable() {
        assertEquals(9, ShortBowVolley.shotCooldownTicks(0.5, 0));
        assertEquals(9, ShortBowVolley.shotCooldownTicks(0.5, 11));
        assertEquals(7, ShortBowVolley.shotCooldownTicks(0.5, 12));
        assertEquals(7, ShortBowVolley.shotCooldownTicks(0.5, 40));
        assertEquals(5, ShortBowVolley.shotCooldownTicks(0.5, 41));
        assertEquals(5, ShortBowVolley.shotCooldownTicks(0.5, 66));
        assertEquals(4, ShortBowVolley.shotCooldownTicks(0.5, 67));
        assertEquals(4, ShortBowVolley.shotCooldownTicks(0.5, 250));
    }

    @Test
    void critChanceIsDividedByFour() {
        ItemStatistics stats = ItemStatistics.builder().withBase(ItemStatistic.CRITICAL_CHANCE, 80.0).build();
        assertEquals(20, ArrowDamage.divideCritChance(stats, 4).getBase(ItemStatistic.CRITICAL_CHANCE), 1e-9);
        assertEquals(80, ArrowDamage.divideCritChance(stats, 1).getBase(ItemStatistic.CRITICAL_CHANCE), 1e-9);
    }

    @Test
    void theBeamAlwaysCrits() {
        ItemStatistics stats = ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, 100.0)
                .withBase(ItemStatistic.CRITICAL_DAMAGE, 100.0)
                .build();
        for (int i = 0; i < 20; i++) {
            Map.Entry<Double, Boolean> hit = PlayerStatistics.runPrimaryDamageFormula(ArrowDamage.alwaysCrit(stats), ItemStatistics.empty());
            assertTrue(hit.getValue());
            assertEquals(200, hit.getKey(), 1e-9);
        }
    }

    @Test
    void salvationNeedsThreeHitsAndSpendsThem() {
        SalvationCharge charge = new SalvationCharge();
        charge.hit();
        charge.hit();
        assertFalse(charge.ready());
        assertFalse(charge.consume());
        charge.hit();
        charge.hit();
        assertEquals(SalvationCharge.HITS_NEEDED, charge.hits());
        assertTrue(charge.ready());
        assertTrue(charge.consume());
        assertEquals(0, charge.hits());
        assertFalse(charge.ready());
    }

    @Test
    void theBeamEntersBoxesAlongItsPath() {
        var origin = new Vec(0, 0, 0);
        var east = new Vec(1, 0, 0);
        assertEquals(10, Salvation.rayEntry(origin, east, Salvation.RANGE,
                new Vec(10, -1, -1), new Vec(25.3, 14.3, 14.3)), 1e-9);
        assertEquals(-1, Salvation.rayEntry(origin, east, Salvation.RANGE,
                new Vec(40, -1, -1), new Vec(55, 14, 14)));
        assertEquals(-1, Salvation.rayEntry(origin, east, Salvation.RANGE,
                new Vec(10, 2, -1), new Vec(25, 14, 14)));
    }
}
