package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueCost;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.ReputationRequirement;
import dev.daweapon.type.skyblockgeneric.shop.type.BitsShopPrice;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CrimsonFoundationTest {

    @Test
    void reputationTitlesMatchTheWikiThresholds() {
        assertEquals(ReputationTier.NEUTRAL, ReputationTier.of(999));
        assertEquals(ReputationTier.FRIENDLY, ReputationTier.of(1_000));
        assertEquals(ReputationTier.TRUSTED, ReputationTier.of(3_000));
        assertEquals(ReputationTier.HONORED, ReputationTier.of(7_000));
        assertEquals(ReputationTier.HERO, ReputationTier.of(50_000));
    }

    @Test
    void everyTitleTogetherIsWorthTheWholeReputationTask() {
        int total = ReputationTier.cumulativeSkyBlockXP(ReputationTier.HERO);
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            assertEquals(faction.getXpSource().getMaxXP(), total);
        }
    }

    @Test
    void explosivityChancesAddUpToTheWholeWikiTable() {
        double total = Arrays.stream(VolcanoExplosivity.values()).mapToDouble(VolcanoExplosivity::getChancePercent).sum();
        assertEquals(100, total, 0.05);
    }

    @Test
    void crimsonIsleDataSurvivesASaveRoundTrip() {
        DatapointCrimsonIsle.CrimsonIsleData data = DatapointCrimsonIsle.CrimsonIsleData.empty();
        data.setFaction(CrimsonFaction.BARBARIAN);
        data.getReputation().put(CrimsonFaction.MAGE, 1_234);
        data.getQuestline().put("BARBARIAN", 3);
        data.getNumbers().put("dojo:best:FORCE", 900L);
        data.getSections().put("faction_quests", "{\"day\":1}");

        String json = DatapointCrimsonIsle.serializer.serialize(data);
        DatapointCrimsonIsle.CrimsonIsleData read = DatapointCrimsonIsle.serializer.deserialize(json);

        assertEquals(CrimsonFaction.BARBARIAN, read.getFaction());
        assertEquals(1_234, read.getReputation(CrimsonFaction.MAGE));
        assertEquals(0, read.getReputation(CrimsonFaction.BARBARIAN));
        assertEquals(3, read.getQuestlineStage("BARBARIAN"));
        assertEquals(900L, read.getNumber("dojo:best:FORCE"));
        assertEquals("{\"day\":1}", read.getSection("faction_quests"));
        assertNull(DatapointCrimsonIsle.serializer.deserialize("{}").getFaction());
    }

    @Test
    void bitsAndEssenceCostsCanBeBought() {
        CatalogueCost bits = CatalogueCost.parse(Map.of("bits", 300));
        assertTrue(bits.purchasable());
        assertEquals(new BitsShopPrice(300), bits.toShopPrice());

        CatalogueCost essence = CatalogueCost.parse(Map.of("essence", Map.of("type", "CRIMSON", "amount", 50)));
        assertTrue(essence.purchasable());
        assertEquals(EssenceType.CRIMSON, essence.essenceKind());

        assertFalse(CatalogueCost.parse(Map.of("essence", Map.of("type", "NOT_REAL", "amount", 5))).purchasable());
    }

    @Test
    void reputationGatesReadTierOrAmount() {
        ReputationRequirement hero = ReputationRequirement.parse(Map.of("faction", "MAGE", "tier", "HERO"));
        assertNotNull(hero);
        assertEquals(12_000, hero.reputation());
        ReputationRequirement exact = ReputationRequirement.parse(Map.of("faction", "barbarian", "reputation", 3_000));
        assertNotNull(exact);
        assertEquals(CrimsonFaction.BARBARIAN, exact.faction());
        assertEquals("<4>❣ <c>Requires <c>12,000 Mage Reputation.", hero.lockLine());
        assertNull(ReputationRequirement.parse(null));
    }
}
