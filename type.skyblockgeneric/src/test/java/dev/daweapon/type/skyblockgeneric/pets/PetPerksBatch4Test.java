package dev.daweapon.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetDrops;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.MobAggroLog;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TrampleHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEquipEffects;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyCondition;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.BingoClimberHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.BulwarkHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.CombatState;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.ExtinguishPools;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetPerksBatch4Test {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                if (YamlFileUtils.loadYaml(file.toFile()).get("items") instanceof List<?> list) {
                    for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
                }
            }
        }
    }

    private static PetComponent component(ItemType type) {
        return new SkyBlockItem(type).getComponent(PetComponent.class);
    }

    private static ConfiguredPetAbility ability(ItemType pet, String name, Rarity rarity) {
        return component(pet).getAbilities().stream()
                .filter(a -> a.getName().equals(name) && a.appliesAt(rarity))
                .reduce((first, second) -> second).orElseThrow();
    }

    private static SkyBlockItem pet(ItemType type, Rarity rarity, int level) {
        SkyBlockItem pet = new SkyBlockItem(type);
        pet.getAttributeHandler().setRarity(rarity);
        PetLevels.setLevel(pet, level);
        return pet;
    }

    private static void assertValues(ConfiguredPetAbility perk, Rarity rarity, double atOne, double atFifty, double atHundred) {
        assertEquals(atOne, perk.valuesAt(rarity, 1)[0], 1e-9, rarity + " L1");
        assertEquals(atFifty, perk.valuesAt(rarity, 50)[0], 1e-9, rarity + " L50");
        assertEquals(atHundred, perk.valuesAt(rarity, 100)[0], 1e-9, rarity + " L100");
    }

    private static final List<Rarity> ALL = List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY);

    private static void assertScaled(ConfiguredPetAbility perk, Rarity rarity, int index, double perLevel, double base) {
        for (int level : new int[]{1, 50, 100}) {
            assertEquals(base + perLevel * level, perk.valuesAt(rarity, level)[index], 1e-9, rarity + " L" + level);
        }
    }

    private static final List<Rarity> BINGO = List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY, Rarity.MYTHIC);

    private static void assertLinear(ItemType pet, String name, Rarity rarity, int index, double base, double perLevel, PetAbilityKind kind) {
        ConfiguredPetAbility perk = ability(pet, name, rarity);
        assertEquals(kind, perk.getKind(), name);
        assertTrue(perk.isActive(), name + " " + rarity);
        for (int level : new int[]{1, 50, 100}) {
            assertEquals(base + perLevel * level, perk.valuesAt(rarity, level)[index], 1e-9, name + " " + rarity + " L" + level);
        }
    }

    private static void assertSteps(ItemType pet, String name, List<Rarity> rarities, double[] perLevel, PetAbilityKind kind) {
        for (int i = 0; i < rarities.size(); i++) assertLinear(pet, name, rarities.get(i), 0, 0, perLevel[i], kind);
    }

    @Test
    void bingoLuckyLootingAtEveryRarity() {
        for (Rarity rarity : BINGO) assertLinear(ItemType.BINGO_PET, "Lucky Looting", rarity, 0, 5, 0.2, PetAbilityKind.TEXT);
        assertEquals(10, MiscPetHooks.collectionAmount(null, ItemType.COBBLESTONE, 10));
        assertEquals(10, PetPerkMath.scaledAmount(10, 0, ThreadLocalRandom.current()));
        int total = 0;
        for (int i = 0; i < 200; i++) total += PetPerkMath.scaledAmount(10, 25, ThreadLocalRandom.current());
        assertTrue(total >= 200 * 12 && total <= 200 * 13, "" + total);
    }

    @Test
    void bingoClimberRaisesHotmAndHotfExperienceOnly() {
        for (Rarity rarity : BINGO) assertLinear(ItemType.BINGO_PET, "Climber", rarity, 0, 100, 1.5, PetAbilityKind.HANDLER);
        PetAbilityHandler handler = new BingoClimberHandler();
        for (int level : new int[]{1, 50, 100}) {
            double value = 100 + 1.5 * level;
            PetAbilityContext context = new PetAbilityContext(null, null, Rarity.COMMON, level, new double[]{value}, null);
            for (PetEvent.XpType type : PetEvent.XpType.values()) {
                PetEvent.XpGain gain = new PetEvent.XpGain(null, null, type, null, 1000);
                handler.onEvent(gain, context);
                boolean boosted = type == PetEvent.XpType.HOTM || type == PetEvent.XpType.HOTF;
                assertEquals(boosted ? 1000 * (1 + value / 100) : 1000, gain.amount(), 1e-9, type + " L" + level);
            }
        }
    }

    @Test
    void bingoFastLearnerFromUncommon() {
        assertFalse(ability(ItemType.BINGO_PET, "Fast Learner", Rarity.UNCOMMON).appliesAt(Rarity.COMMON));
        for (Rarity rarity : BINGO.subList(1, BINGO.size())) {
            assertLinear(ItemType.BINGO_PET, "Fast Learner", rarity, 0, 5, 0.1, PetAbilityKind.TEXT);
        }
        assertEquals(1, MiscPetHooks.learningMultiplier(null), 1e-9);
    }

    @Test
    void bingoChimeraScalesTheSummonedPetsBaseStats() {
        for (Rarity rarity : BINGO.subList(2, BINGO.size())) {
            assertLinear(ItemType.BINGO_PET, "Chimera", rarity, 0, 10, 0.3, PetAbilityKind.HANDLER);
        }
        ConfiguredPetAbility chimera = ability(ItemType.BINGO_PET, "Chimera", Rarity.LEGENDARY);
        ItemStatistics base = component(ItemType.LION_PET).statisticsAt(Rarity.LEGENDARY, 100);
        ItemStatistics scaled = dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.BaseStatBoostHandler
                .scaled(base, chimera.valuesAt(Rarity.LEGENDARY, 100)[0] / 100D);
        for (ItemStatistic stat : ItemStatistic.values()) {
            assertEquals(base.getOverall(stat) * 0.4, scaled.getOverall(stat), 1e-9, stat.name());
        }
        assertTrue(chimera.getStatistics(null, pet(ItemType.BINGO_PET, Rarity.LEGENDARY, 100)) != null);
    }

    @Test
    void bingoScavengerPaysPerMobLevel() {
        for (Rarity rarity : BINGO.subList(3, BINGO.size())) {
            assertLinear(ItemType.BINGO_PET, "Scavenger", rarity, 0, 0.1, 0.009, PetAbilityKind.HANDLER);
        }
        assertEquals(0, PetPerkMath.coinsPerMobLevel(1, null));
        assertEquals(0, PetPerkMath.coinsPerMobLevel(1, 0));
        assertEquals(100, PetPerkMath.coinsPerMobLevel(1, 100));
        assertEquals(5, PetPerkMath.coinsPerMobLevel(0.1 + 0.009 * 50, 10));
        assertEquals(1, PetPerkMath.coinsPerMobLevel(0.109, 10));
    }

    @Test
    void bingoConsumerAddsPotionTime() {
        for (Rarity rarity : BINGO.subList(4, BINGO.size())) {
            assertLinear(ItemType.BINGO_PET, "Consumer", rarity, 0, 10, 0.4, PetAbilityKind.TEXT);
        }
        assertEquals(1, MiscPetHooks.potionDurationMultiplier(null), 1e-9);
        assertEquals(1, MiscPetHooks.godPotionDurationMultiplier(null), 1e-9);
    }

    @Test
    void owlTrainingRefundsGrowWithSpendUpToFivePercent() {
        ConfiguredPetAbility perk = ability(ItemType.OWL_PET, "Training Refunds", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertEquals(0, PetPerkMath.trainingRefundPercent(999_999));
        assertEquals(1, PetPerkMath.trainingRefundPercent(1_000_000));
        assertEquals(3, PetPerkMath.trainingRefundPercent(3_500_000));
        assertEquals(5, PetPerkMath.trainingRefundPercent(5_000_000));
        assertEquals(5, PetPerkMath.trainingRefundPercent(500_000_000));
        assertEquals(9_500_000, PetPerkMath.discountedTrainingCost(10_000_000, PetPerkMath.trainingRefundPercent(10_000_000)));
        assertEquals(2_000_000, PetPerkMath.discountedTrainingCost(2_000_000, 0));
        assertEquals(0, MiscPetHooks.trainingRefundPercent(null, 5_000_000), 1e-9);
    }

    @Test
    void wispBulwarkTiersFollowTheWiki() {
        ItemType[] wisps = {ItemType.DROPLET_WISP_PET, ItemType.FROST_WISP_PET, ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET};
        Rarity[] rarities = {Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY};
        for (int i = 0; i < wisps.length; i++) {
            ConfiguredPetAbility perk = ability(wisps[i], "Bulwark", rarities[i]);
            assertEquals(PetAbilityKind.HANDLER, perk.getKind());
            assertTrue(perk.isActive());
        }
        assertEquals(0, PetPerkMath.bulwarkDefense(99));
        assertEquals(30, PetPerkMath.bulwarkDefense(100));
        assertEquals(3, PetPerkMath.bulwarkTrueDefense(100));
        assertEquals(135, PetPerkMath.bulwarkDefense(500));
        assertEquals(14, PetPerkMath.bulwarkTrueDefense(799));
        assertEquals(180, PetPerkMath.bulwarkDefense(800));
        assertEquals(500, PetPerkMath.bulwarkDefense(25_000));
        assertEquals(600, PetPerkMath.bulwarkDefense(200_000));
        assertEquals(60, PetPerkMath.bulwarkTrueDefense(999_999));
        assertEquals(100, PetPerkMath.bulwarkNextKills(0));
        assertEquals(-1, PetPerkMath.bulwarkNextKills(200_000));
        assertEquals(60, PetPerkMath.bulwarkNextDefense(100));
    }

    @Test
    void bulwarkReducesDamageOnTopOfDefenseAndTrueDefense() {
        assertEquals(100, PetPerkMath.bulwarkDamage(100, 50, 0, 0), 1e-9);
        assertEquals(100 * 150D / 180, PetPerkMath.bulwarkDamage(100, 50, 30, 0), 1e-9);
        assertEquals(100 * 100D / 103, PetPerkMath.bulwarkDamage(100, 0, 0, 3), 1e-9);
        assertTrue(PetPerkMath.bulwarkDamage(100, 200, 600, 60) < PetPerkMath.bulwarkDamage(100, 200, 30, 3));
    }

    @Test
    void bulwarkKillsPersistOnThePetAndOldSavesLoadAtZero() {
        for (Rarity rarity : List.of(Rarity.UNCOMMON, Rarity.RARE)) {
            SkyBlockItem wisp = pet(rarity == Rarity.RARE ? ItemType.FROST_WISP_PET : ItemType.DROPLET_WISP_PET, rarity, 100);
            assertEquals(0, wisp.getAttributeHandler().getPetBulwarkKills());
            wisp.getAttributeHandler().setPetBulwarkKills(150);
            SkyBlockItem loaded = new SkyBlockItem(new UnderstandableSkyBlockItemSerializer()
                    .deserialize(new UnderstandableSkyBlockItemSerializer().serialize(wisp.toUnderstandable())));
            assertEquals(150, loaded.getAttributeHandler().getPetBulwarkKills());
            assertEquals(rarity, loaded.getAttributeHandler().getRarity());

            UnderstandableSkyBlockItem full = wisp.toUnderstandable();
            List<ItemAttribute> without = full.attributes().stream()
                    .filter(attribute -> !attribute.getKey().equals("pet_bulwark_kills")).toList();
            String json = new UnderstandableSkyBlockItemSerializer().serialize(
                    new UnderstandableSkyBlockItem(full.itemKey(), without, full.amount(), full.material()));
            SkyBlockItem old = new SkyBlockItem(new UnderstandableSkyBlockItemSerializer().deserialize(json));
            assertEquals(0, old.getAttributeHandler().getPetBulwarkKills());
        }
    }

    @Test
    void bulwarkLoreShowsTheBonusAndTheNextTier() {
        SkyBlockItem wisp = pet(ItemType.SUBZERO_WISP_PET, Rarity.LEGENDARY, 100);
        wisp.getAttributeHandler().setPetBulwarkKills(150);
        List<String> lore = component(ItemType.SUBZERO_WISP_PET).getAbsoluteLore(null, wisp);
        assertTrue(lore.stream().anyMatch(line -> line.contains("+30") && line.contains("+3")), lore.toString());
        assertTrue(lore.stream().anyMatch(line -> line.contains("Next Upgrade") && line.contains("+60") && line.contains("150")), lore.toString());
        assertEquals(3, BulwarkHandler.lines(200_000).size());
        assertEquals(4, BulwarkHandler.lines(0).size());
    }

    @Test
    void extinguishHealsByRarityAndBathingNeedsToStandInThePool() {
        double[][] values = {{15, 4}, {20, 7}, {25, 10}};
        ItemType[] wisps = {ItemType.FROST_WISP_PET, ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET};
        Rarity[] rarities = {Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY};
        for (int i = 0; i < wisps.length; i++) {
            ConfiguredPetAbility perk = ability(wisps[i], "Extinguish", rarities[i]);
            assertEquals(PetAbilityKind.HANDLER, perk.getKind());
            assertTrue(perk.isActive());
            for (int level : new int[]{1, 50, 100}) {
                assertEquals(values[i][0], perk.valuesAt(rarities[i], level)[0], 1e-9);
                assertEquals(values[i][1], perk.valuesAt(rarities[i], level)[1], 1e-9);
            }
        }
        ExtinguishPools.Strength strength = new ExtinguishPools.Strength(25, 10);
        assertEquals(250, strength.instantAmount(1000), 1e-9);
        assertEquals(50, strength.regenAmount(1000), 1e-9);
        Pos pool = new Pos(10, 70, 10);
        assertTrue(ExtinguishPools.isBathing(pool, new Pos(11, 70, 11)));
        assertFalse(ExtinguishPools.isBathing(pool, new Pos(15, 70, 10)));
        assertFalse(ExtinguishPools.isBathing(pool, new Pos(10, 80, 10)));
        assertEquals(8_000, PetPerkMath.EXTINGUISH_INTERVAL_MS);
        assertEquals(ExtinguishPools.Strength.NONE, new ExtinguishPools.Strength(0, 0));
    }

    @Test
    void combatStateExpiresAfterTheWindow() {
        java.util.UUID id = java.util.UUID.randomUUID();
        assertFalse(CombatState.inCombat(id, 1_000, PetPerkMath.EXTINGUISH_COMBAT_MS));
        CombatState.record(id, 1_000);
        assertTrue(CombatState.inCombat(id, 10_999, PetPerkMath.EXTINGUISH_COMBAT_MS));
        assertFalse(CombatState.inCombat(id, 11_000, PetPerkMath.EXTINGUISH_COMBAT_MS));
        CombatState.release(id);
        assertFalse(CombatState.inCombat(id, 1_001, PetPerkMath.EXTINGUISH_COMBAT_MS));
    }

    @Test
    void armadilloRollingMinerRechargesBySecondsPerRarityAndLevel() {
        double[] perLevel = {-0.2, -0.3, -0.4, -0.4};
        List<Rarity> rarities = List.of(Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY, Rarity.MYTHIC);
        for (int i = 0; i < rarities.size(); i++) {
            assertLinear(ItemType.ARMADILLO_PET, "Rolling Miner", rarities.get(i), 0, 60, perLevel[i], PetAbilityKind.TEXT);
        }
        assertEquals(40, ability(ItemType.ARMADILLO_PET, "Rolling Miner", Rarity.RARE).valuesAt(Rarity.RARE, 100)[0], 1e-9);
        assertEquals(20, ability(ItemType.ARMADILLO_PET, "Rolling Miner", Rarity.LEGENDARY).valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertTrue(PetPerkMath.offCooldown(0, 1, 40_000));
        assertFalse(PetPerkMath.offCooldown(1_000, 40_999, 40_000));
        assertTrue(PetPerkMath.offCooldown(1_000, 41_000, 40_000));
        assertFalse(MiningPetBonuses.rollingMinerReady(null, 1));
    }

    @Test
    void endermiteDailyCommuterCutsTransmissionMana() {
        List<Rarity> rarities = List.of(Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY, Rarity.MYTHIC);
        double[] perLevel = {0.3, 0.4, 0.4, 0.4};
        for (int i = 0; i < rarities.size(); i++) {
            assertLinear(ItemType.ENDERMITE_PET, "Daily Commuter", rarities.get(i), 0, 0, perLevel[i], PetAbilityKind.TEXT);
        }
        assertEquals(0.6, PetPerkMath.manaCostFactor(40), 1e-9);
        assertEquals(1, PetPerkMath.manaCostFactor(0), 1e-9);
        assertEquals(0, PetPerkMath.manaCostFactor(150), 1e-9);
        assertEquals(1, MiscPetHooks.transmissionManaMultiplier(null), 1e-9);
    }

    @Test
    void glacitePowderAndGemstonePowderBonuses() {
        assertSteps(ItemType.GLACITE_GOLEM_PET, "Powder-powered", List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY),
                new double[]{0.1, 0.15, 0.15, 0.2, 0.2}, PetAbilityKind.TEXT);
        assertLinear(ItemType.SCATHA_PET, "Bejeweled Eyes", Rarity.LEGENDARY, 0, 0, 0.2, PetAbilityKind.TEXT);
        assertFalse(ability(ItemType.SCATHA_PET, "Bejeweled Eyes", Rarity.LEGENDARY).appliesAt(Rarity.EPIC));
        assertEquals(100, PetPerkMath.scaledAmount(100L, 0, ThreadLocalRandom.current()));
        long total = 0;
        for (int i = 0; i < 500; i++) total += PetPerkMath.scaledAmount(10L, 12, ThreadLocalRandom.current());
        assertTrue(total >= 500 * 11 && total <= 500 * 12, "" + total);
    }

    @Test
    void silverfishMagneticBoostsMiningSkillExperience() {
        assertSteps(ItemType.SILVERFISH_PET, "Magnetic", List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY),
                new double[]{0.3, 0.4, 0.4, 0.5, 0.5}, PetAbilityKind.TEXT);
        assertEquals(110, PetPerkMath.boosted(100, 10), 1e-9);
        assertEquals(150, PetPerkMath.boosted(100, 0.5 * 100), 1e-9);
    }

    @Test
    void goblinFreeRangeEggsRaiseOnlyTheRareEggChance() {
        assertLinear(ItemType.GOBLIN_PET, "Free-range Eggs", Rarity.LEGENDARY, 0, 0, 0.5, PetAbilityKind.TEXT);
        assertEquals(0.04 * 1.5, PetPerkMath.boostChance(0.04, 50), 1e-9);
        assertEquals(0.04, PetPerkMath.boostChance(0.04, 0), 1e-9);
        assertEquals(0.04, MiningPetBonuses.goblinEggChance(null, null, ItemType.RED_GOBLIN_EGG, 0.04), 1e-9);
    }

    @Test
    void crystalHollowsPerksAreWiredOrSayWhyNot() {
        for (String perk : new String[]{"Dispersion", "Cannibal"}) {
            assertEquals(PetAbilityKind.TEXT, ability(ItemType.BAL_PET, perk, Rarity.LEGENDARY).getKind());
            assertEquals(null, ability(ItemType.BAL_PET, perk, Rarity.LEGENDARY).getUnavailable());
        }
        assertEquals(PetAbilityKind.STAT, ability(ItemType.BAL_PET, "Furnace", Rarity.LEGENDARY).getKind());
        assertEquals(PetAbilityKind.TEXT, ability(ItemType.MOLE_PET, "Magnetic Nose", Rarity.LEGENDARY).getKind());
        assertEquals(PetAbilityKind.HANDLER, ability(ItemType.AMMONITE_PET, "Expert Cave Fisher", Rarity.LEGENDARY).getKind());
        assertEquals("Needs pet riding", ability(ItemType.ARMADILLO_PET, "Tunneller", Rarity.COMMON).getUnavailable());
    }

    @Test
    void witherSkeletonDeathsTouchDealsAThirdPerSecondAndNeverStacks() {
        for (int level : new int[]{1, 50, 100}) {
            assertEquals(2.0 * level, ability(ItemType.WITHER_SKELETON_PET, "Death's Touch", Rarity.LEGENDARY).valuesAt(Rarity.LEGENDARY, level)[0], 1e-9);
        }
        ConfiguredPetAbility perk = ability(ItemType.WITHER_SKELETON_PET, "Death's Touch", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.HANDLER, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertEquals(1000 * 0.2 / 3, PetPerkMath.witherDamagePerSecond(1000, 20), 1e-9);
        assertEquals(0, PetPerkMath.witherDamagePerSecond(1000, 0), 1e-9);
        UUID mob = UUID.randomUUID();
        long now = 5_000_000;
        assertTrue(MobStatusEffects.tryWither(mob, 3_000, now));
        assertFalse(MobStatusEffects.tryWither(mob, 3_000, now + 2_999));
        assertTrue(MobStatusEffects.tryWither(mob, 3_000, now + 3_000));
    }

    @Test
    void orchidMantisOrchidNectarDropsJellyOrPlantMatter() {
        for (Rarity rarity : List.of(Rarity.LEGENDARY)) {
            ConfiguredPetAbility perk = ability(ItemType.ORCHID_MANTIS_PET, "Orchid Nectar", rarity);
            assertEquals(PetAbilityKind.DROP, perk.getKind());
            assertTrue(perk.isActive());
            assertEquals(List.of(ItemType.JELLY, ItemType.PLANT_MATTER), perk.getItems());
            assertLinear(ItemType.ORCHID_MANTIS_PET, "Orchid Nectar", rarity, 0, 0, 0.0002, PetAbilityKind.DROP);
        }
        assertEquals(Rarity.UNCOMMON, ItemType.JELLY.rarity);
        assertEquals(net.minestom.server.item.Material.PAPER, ItemType.JELLY.material);
    }

    @Test
    void slugSlowAndSteadyShortensTheSlugfishBobber() {
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            assertLinear(ItemType.SLUG_PET, "Slow and Steady", rarity, 0, 0, 0.5, PetAbilityKind.TEXT);
        }
        assertEquals(20, PetPerkMath.reducedBobberSeconds(20, 0), 1e-9);
        assertEquals(15, PetPerkMath.reducedBobberSeconds(20, 25), 1e-9);
        assertEquals(10, PetPerkMath.reducedBobberSeconds(20, 50), 1e-9);
        TrophyCondition slugfish = new TrophyCondition.MinBobberSeconds(20);
        assertFalse(slugfish.test(castFor(19_999)));
        assertTrue(slugfish.test(castFor(20_000)));
        assertEquals(0, MiscPetHooks.slugfishBobberPercent(null), 1e-9);
    }

    private static FishingContext castFor(long millis) {
        return new FishingContext(null, null, FishingMedium.LAVA, null, null, null, null, null, new Pos(0, 0, 0), false,
                ItemStatistics.empty(), millis);
    }

    @Test
    void parrotBirdDiscourseGivesStrengthToTheBestParrotInRange() {
        for (int level : new int[]{1, 50, 100}) {
            assertEquals(5 + 0.25 * level, ability(ItemType.PARROT_PET, "Bird Discourse", Rarity.LEGENDARY).valuesAt(Rarity.LEGENDARY, level)[0], 1e-9);
        }
        ConfiguredPetAbility perk = ability(ItemType.PARROT_PET, "Bird Discourse", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertTrue(PetPerkMath.inAuraRange(0));
        assertTrue(PetPerkMath.inAuraRange(20));
        assertFalse(PetPerkMath.inAuraRange(20.01));
        assertEquals(30, PetPerkMath.bestAura(30, 10), 1e-9);
        assertEquals(30, PetPerkMath.bestAura(10, 30), 1e-9);
    }

    @Test
    void sheepOverhealShieldAfterTenQuietSeconds() {
        for (Rarity rarity : List.of(Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY)) {
            assertLinear(ItemType.SHEEP_PET, "Overheal", rarity, 0, 0, 0.1, PetAbilityKind.TEXT);
        }
        assertFalse(ability(ItemType.SHEEP_PET, "Overheal", Rarity.RARE).appliesAt(Rarity.UNCOMMON));
        assertEquals(10, PetPerkMath.overhealShield(100, 10), 1e-4);
        assertEquals(1000, PetPerkMath.overhealShield(10_000, 10), 1e-4);
        assertEquals(0, PetPerkMath.overhealShield(10_000, 0), 1e-4);
        assertFalse(PetPerkMath.overheals(1_000, 10_999, false));
        assertTrue(PetPerkMath.overheals(1_000, 11_000, false));
        assertFalse(PetPerkMath.overheals(1_000, 60_000, true));
    }

    @Test
    void rabbitChocolateInjectionsAddsToTheProductionMultiplier() {
        for (Rarity rarity : List.of(Rarity.MYTHIC)) {
            assertLinear(ItemType.RABBIT_PET, "Chocolate Injections", rarity, 0, 0.01, 0.0004, PetAbilityKind.TEXT);
        }
        assertFalse(ability(ItemType.RABBIT_PET, "Chocolate Injections", Rarity.MYTHIC).appliesAt(Rarity.LEGENDARY));
        assertEquals(1.05, PetPerkMath.chocolateMultiplier(1, 0.05), 1e-9);
        assertEquals(1.7, PetPerkMath.chocolateMultiplier(1.7, 0), 1e-9);
        assertEquals(1, PetPerkMath.chocolateMultiplier(1, -3), 1e-9);
        assertEquals(0, MiscPetHooks.chocolateMultiplierBonus(null), 1e-9);
        DatapointChocolateFactory.ChocolateFactoryData data = new DatapointChocolateFactory.ChocolateFactoryData();
        data.setEmployee(DatapointChocolateFactory.EmployeeType.values()[0], 5);
        double base = data.getChocolatePerSecond();
        data.setPetMultiplier(0.05);
        assertEquals(base * 1.05, data.getChocolatePerSecond(), 1e-9);
    }

    @Test
    void jellyIsAnUncommonBazaarItem() {
        assertNotNull(ItemType.JELLY);
        assertEquals(Rarity.UNCOMMON, ItemType.JELLY.rarity);
    }

    @Test
    void everyBatchFourPerkIsActive() {
        String[][] perks = {
                {"BINGO_PET", "Lucky Looting"}, {"BINGO_PET", "Climber"}, {"BINGO_PET", "Fast Learner"}, {"BINGO_PET", "Chimera"},
                {"BINGO_PET", "Scavenger"}, {"BINGO_PET", "Consumer"}, {"OWL_PET", "Training Refunds"},
                {"DROPLET_WISP_PET", "Bulwark"}, {"FROST_WISP_PET", "Extinguish"}, {"ARMADILLO_PET", "Rolling Miner"},
                {"ENDERMITE_PET", "Daily Commuter"}, {"GLACITE_GOLEM_PET", "Powder-powered"}, {"SCATHA_PET", "Bejeweled Eyes"},
                {"SILVERFISH_PET", "Magnetic"}, {"GOBLIN_PET", "Free-range Eggs"}, {"WITHER_SKELETON_PET", "Death's Touch"},
                {"ORCHID_MANTIS_PET", "Orchid Nectar"}, {"SLUG_PET", "Slow and Steady"}, {"PARROT_PET", "Bird Discourse"},
                {"SHEEP_PET", "Overheal"}, {"RABBIT_PET", "Chocolate Injections"}};
        for (String[] perk : perks) {
            ItemType type = ItemType.valueOf(perk[0]);
            Rarity top = component(type).getRarities().stream().max(java.util.Comparator.comparingInt(Rarity::ordinal)).orElseThrow();
            assertTrue(ability(type, perk[1], top).isActive(), perk[0] + " " + perk[1]);
        }
    }
}
