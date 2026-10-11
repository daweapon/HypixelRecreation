package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetDrops;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import net.swofty.type.skyblockgeneric.entity.mob.MobStatusEffects;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.MobAggroLog;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.TrampleHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.user.pets.PetEquipEffects;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
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

class PetPerksBatch3Test {
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

    @Test
    void blueWhaleIngestReducesDamageWhileAbsorbed() {
        for (Rarity rarity : ALL) {
            ConfiguredPetAbility perk = ability(ItemType.BLUE_WHALE_PET, "Ingest", rarity);
            assertEquals(PetAbilityKind.DAMAGE_REDUCTION, perk.getKind());
            assertTrue(perk.isActive());
            assertScaled(perk, rarity, 0, 0.05, 0);
        }
        assertTrue(PetConditions.isNamed("ABSORPTION_ACTIVE"));
    }

    @Test
    void babyYetiFrostyFamiliarityOnlyAgainstWinterSeaCreatures() {
        ConfiguredPetAbility perk = ability(ItemType.BABY_YETI_PET, "Frosty Familiarity", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertScaled(perk, Rarity.LEGENDARY, 0, 0.1, 0);
        assertEquals(0, GatheringPetBonuses.against(null, null).getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
    }

    @Test
    void hermitCrabHotspotHazardRaisesTheHotspotChanceUpToTheCap() {
        ConfiguredPetAbility perk = ability(ItemType.HERMIT_CRAB_PET, "Hotspot Hazard", Rarity.MYTHIC);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertScaled(perk, Rarity.MYTHIC, 0, 0.2, 0);
        for (int level : new int[]{1, 50, 100}) {
            double bonus = perk.valuesAt(Rarity.MYTHIC, level)[0];
            assertEquals(15 + bonus, PetPerkMath.hotspotChance(15, bonus, PetPerkMath.HOTSPOT_CHANCE_CAP), 1e-9);
        }
        assertEquals(35, PetPerkMath.hotspotChance(15, 20, 85), 1e-9);
        assertEquals(85, PetPerkMath.hotspotChance(35, 80, 85), 1e-9);
        assertEquals(15, PetPerkMath.hotspotChance(15, 0, 85), 1e-9);
    }

    @Test
    void squidMoreInkChanceByRarityAndLevel() {
        double[] perLevel = {0.5, 0.75, 0.75, 1, 1};
        for (int i = 0; i < ALL.size(); i++) {
            ConfiguredPetAbility perk = ability(ItemType.SQUID_PET, "More Ink", ALL.get(i));
            assertEquals(PetAbilityKind.TEXT, perk.getKind());
            assertTrue(perk.isActive());
            assertScaled(perk, ALL.get(i), 0, perLevel[i], 0);
        }
        assertTrue(PetPerkMath.doublesSquidDrops("SQUID"));
        assertTrue(PetPerkMath.doublesSquidDrops("NIGHT_SQUID"));
        assertFalse(PetPerkMath.doublesSquidDrops("SEA_WALKER"));
        assertEquals(7, GatheringPetBonuses.squidDropAmount(null, null, 7));
    }

    @Test
    void dolphinSplashSurpriseStunsForFiveSeconds() {
        ConfiguredPetAbility perk = ability(ItemType.DOLPHIN_PET, "Splash Surprise", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertEquals(5_000, PetPerkMath.SPLASH_SURPRISE_STUN_MS);
        UUID mob = UUID.randomUUID();
        long now = 1_000_000;
        MobStatusEffects.markStunned(mob, PetPerkMath.SPLASH_SURPRISE_STUN_MS, now);
        assertTrue(MobStatusEffects.isStunned(mob, now + 4_999));
        assertFalse(MobStatusEffects.isStunned(mob, now + 5_000));
    }

    @Test
    void penguinThickBlubberScalesWithLevel() {
        ConfiguredPetAbility perk = ability(ItemType.PENGUIN_PET, "Thick Blubber", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertScaled(perk, Rarity.LEGENDARY, 0, 0.05, 1);
    }

    @Test
    void ocelotTreeEssenceChancePerLevel() {
        ConfiguredPetAbility perk = ability(ItemType.OCELOT_PET, "Tree Essence", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertScaled(perk, Rarity.LEGENDARY, 0, 0.3, 0);
        assertFalse(GatheringPetBonuses.treeEssenceProcs(null));
    }

    @Test
    void slothSlowStartScalesAndRechargesEverySixtySeconds() {
        double[] sweep = {0.15, 0.2, 0.2, 0.25, 0.25};
        double[] fortune = {0.3, 0.4, 0.4, 0.5, 0.5};
        for (int i = 0; i < ALL.size(); i++) {
            ConfiguredPetAbility perk = ability(ItemType.SLOTH_PET, "Slow Start", ALL.get(i));
            assertEquals(PetAbilityKind.TEXT, perk.getKind());
            assertTrue(perk.isActive());
            assertScaled(perk, ALL.get(i), 0, sweep[i], 0);
            assertScaled(perk, ALL.get(i), 1, fortune[i], 0);
        }
        assertTrue(PetPerkMath.offCooldown(0, 5, PetPerkMath.SLOW_START_COOLDOWN_MS));
        assertFalse(PetPerkMath.offCooldown(1_000, 60_999, PetPerkMath.SLOW_START_COOLDOWN_MS));
        assertTrue(PetPerkMath.offCooldown(1_000, 61_000, PetPerkMath.SLOW_START_COOLDOWN_MS));
        assertEquals(GatheringPetBonuses.SlowStart.NONE, GatheringPetBonuses.consumeSlowStart(null));
    }

    @Test
    void contrabandCatchesDoNotRollTheSquidPetTwice() {
        CatchPayload seaCreature = new CatchPayload.SeaCreature("SQUID", 0);
        CatchPayload treasure = new CatchPayload.Treasure("COINS", 1, 0, "GOOD");
        GatheringPetDrops.fishCaught(null, FishingMedium.WATER, new CatchPayload.Multi(List.of(seaCreature, treasure)));
        GatheringPetDrops.fishCaught(null, FishingMedium.WATER, seaCreature);
    }
}
