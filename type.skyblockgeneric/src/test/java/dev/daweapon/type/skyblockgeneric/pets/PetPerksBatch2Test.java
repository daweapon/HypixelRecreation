package dev.daweapon.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
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

class PetPerksBatch2Test {
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

    @Test
    void crowQuickHandsLowersCooldownsByRarityAndLevel() {
        for (Rarity rarity : List.of(Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE)) {
            ConfiguredPetAbility perk = ability(ItemType.CROW_PET, "Quick Hands", rarity);
            assertEquals(PetAbilityKind.TEXT, perk.getKind());
            assertTrue(perk.isActive());
            assertValues(perk, rarity, 3.07, 6.5, 10);
        }
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            assertValues(ability(ItemType.CROW_PET, "Quick Hands", rarity), rarity, 3.12, 9, 15);
        }
        assertEquals(97, PetPerkMath.reducedCooldown(100, 3.07));
        assertEquals(90, PetPerkMath.reducedCooldown(100, 10));
        assertEquals(85, PetPerkMath.reducedCooldown(100, 15));
        assertEquals(100, PetPerkMath.reducedCooldown(100, 0));
        assertEquals(0, PetPerkMath.reducedCooldown(100, 150));
        assertEquals(0, PetPerkMath.reducedCooldown(0, 15));
        assertEquals(100, CombatPetHooks.cooldownTicks(null, 100));
    }

    @Test
    void horseHighStrideGrantsJumpBoostByRarity() {
        int[] expected = {2, 3, 3, 4, 4};
        Rarity[] rarities = {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY};
        for (int i = 0; i < rarities.length; i++) {
            ConfiguredPetAbility perk = ability(ItemType.HORSE_PET, "High Stride", rarities[i]);
            assertTrue(perk.isActive());
            for (int level : new int[]{1, 50, 100}) assertEquals(expected[i], perk.valuesAt(rarities[i], level)[0], 1e-9);
        }
        ConfiguredPetAbility skeletonHorse = ability(ItemType.SKELETON_HORSE_PET, "High Stride", Rarity.LEGENDARY);
        assertTrue(skeletonHorse.isActive());
        for (int level : new int[]{1, 50, 100}) assertEquals(4, skeletonHorse.valuesAt(Rarity.LEGENDARY, level)[0], 1e-9);
        assertEquals(0.62, PetPerkMath.jumpStrength(2), 1e-9);
        assertEquals(0.72, PetPerkMath.jumpStrength(3), 1e-9);
        assertEquals(0.82, PetPerkMath.jumpStrength(4), 1e-9);
        assertEquals(PetPerkMath.BASE_JUMP_STRENGTH, PetPerkMath.jumpStrength(0), 1e-9);
        PetEquipEffects.apply(null);
    }

    @Test
    void trampleScalesForBothHorsesAndNeedsTwentyBlocks() {
        ConfiguredPetAbility horse = ability(ItemType.HORSE_PET, "Trample", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.HANDLER, horse.getKind());
        assertTrue(horse.isActive());
        assertFalse(horse.appliesAt(Rarity.EPIC));
        assertValues(horse, Rarity.LEGENDARY, 0.545, 2.75, 5);

        ConfiguredPetAbility skeletonHorse = ability(ItemType.SKELETON_HORSE_PET, "Trample", Rarity.LEGENDARY);
        assertTrue(skeletonHorse.isActive());
        assertValues(skeletonHorse, Rarity.LEGENDARY, 0.8125, 3.875, 7);

        assertEquals(200, PetPerkMath.trampleDamage(200, 5, 20), 1e-9);
        assertEquals(420, PetPerkMath.trampleDamage(200, 7, 30), 1e-9);
        assertEquals(0, PetPerkMath.trampleDamage(200, 5, 19), 1e-9);
        assertTrue(PetPerkMath.tramples(20));
        assertFalse(PetPerkMath.tramples(19));
    }

    @Test
    void trampleLeavesShortFallsAlone() {
        SkyBlockItem horse = pet(ItemType.HORSE_PET, Rarity.LEGENDARY, 100);
        PetEvent.FallDamage fall = new PetEvent.FallDamage(null, horse, 36, 19);
        PetAbilityContext context = new PetAbilityContext(horse, null, Rarity.LEGENDARY, 100, new double[]{5}, null);
        new TrampleHandler().onEvent(fall, context);
        assertEquals(36, fall.damage(), 1e-9);
        assertEquals(19, fall.distance());
        assertEquals(0, new PetEvent.FallDamage(null, horse, 4).distance());
    }

    @Test
    void phoenixMagicBirdIsVerySpecialOnly() {
        ConfiguredPetAbility perk = ability(ItemType.PHOENIX_PET, "Magic Bird", Rarity.VERY_SPECIAL);
        assertEquals(PetAbilityKind.TEXT, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.SPECIAL));
        
    }

    @Test
    void kuudraSpecialistIsAFlatTwentyPercentAtLegendary() {
        ConfiguredPetAbility perk = ability(ItemType.KUUDRA_PET, "Kuudra Specialist", Rarity.LEGENDARY);
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertValues(perk, Rarity.LEGENDARY, 20, 20, 20);
        assertEquals(1, CombatPetHooks.kuudraDamageMultiplier(null), 1e-9);
    }

    @Test
    void skeletalDefenseHitsThirtyTimesCritDamageEveryFiveSeconds() {
        ConfiguredPetAbility perk = ability(ItemType.SKELETON_PET, "Skeletal Defense", Rarity.LEGENDARY);
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.EPIC));
        assertEquals(3000, PetPerkMath.skeletalDefenseDamage(100), 1e-9);
        assertEquals(9000, PetPerkMath.skeletalDefenseDamage(300), 1e-9);
        assertEquals(0, PetPerkMath.skeletalDefenseDamage(-5), 1e-9);
        assertTrue(PetPerkMath.offCooldown(0, 1, PetPerkMath.SKELETAL_DEFENSE_COOLDOWN_MS));
        assertFalse(PetPerkMath.offCooldown(1_000, 5_999, PetPerkMath.SKELETAL_DEFENSE_COOLDOWN_MS));
        assertTrue(PetPerkMath.offCooldown(1_000, 6_000, PetPerkMath.SKELETAL_DEFENSE_COOLDOWN_MS));
        CombatPetHooks.petTick(null);
    }

    @Test
    void tigerHemorrhageCutsHealingAndExpiresAfterTenSeconds() {
        ConfiguredPetAbility perk = ability(ItemType.TIGER_PET, "Hemorrhage", Rarity.RARE);
        assertEquals(PetAbilityKind.HANDLER, perk.getKind());
        assertTrue(perk.isActive());
        assertFalse(perk.appliesAt(Rarity.UNCOMMON));
        assertValues(perk, Rarity.RARE, 0.3, 15, 30);
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) assertValues(ability(ItemType.TIGER_PET, "Hemorrhage", rarity), rarity, 0.55, 27.5, 55);

        UUID mob = UUID.randomUUID();
        long now = 1_000_000;
        assertEquals(100, MobStatusEffects.healing(mob, 100, now), 1e-9);
        MobStatusEffects.reduceHealing(mob, 55, PetPerkMath.HEMORRHAGE_MS, now);
        assertEquals(45, MobStatusEffects.healing(mob, 100, now + 9_999), 1e-9);
        MobStatusEffects.reduceHealing(mob, 30, PetPerkMath.HEMORRHAGE_MS, now + 100);
        assertEquals(45, MobStatusEffects.healing(mob, 100, now + 200), 1e-9);
        assertEquals(100, MobStatusEffects.healing(mob, 100, now + 10_000), 1e-9);
        assertEquals(0, PetPerkMath.healingAfterReduction(80, 100), 1e-9);
        assertEquals(80, PetPerkMath.healingAfterReduction(80, -20), 1e-9);
    }

    @Test
    void tyrannosaurusFerociousRoarStunsForTenSeconds() {
        ConfiguredPetAbility perk = ability(ItemType.TYRANNOSAURUS_PET, "Ferocious Roar", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.HANDLER, perk.getKind());
        assertTrue(perk.isActive());
        assertValues(perk, Rarity.LEGENDARY, 0.1, 5, 10);

        UUID mob = UUID.randomUUID();
        long now = 5_000_000;
        assertFalse(MobStatusEffects.isStunned(mob, now));
        MobStatusEffects.markStunned(mob, PetPerkMath.STUN_MS, now);
        assertTrue(MobStatusEffects.isStunned(mob, now + 1));
        assertTrue(MobStatusEffects.isStunned(mob, now + 9_999));
        assertFalse(MobStatusEffects.isStunned(mob, now + 10_000));
        assertTrue(PetPerkMath.offCooldown(now, now + 10_000, PetPerkMath.STUN_COOLDOWN_MS));
        assertFalse(PetPerkMath.offCooldown(now, now + 9_999, PetPerkMath.STUN_COOLDOWN_MS));
    }

    @Test
    void turtleGeniusAmniotePercentGrowsWithLevel() {
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            ConfiguredPetAbility perk = ability(ItemType.TURTLE_PET, "Genius Amniote", rarity);
            assertEquals(PetAbilityKind.TEXT, perk.getKind());
            assertTrue(perk.isActive());
            assertValues(perk, rarity, 1.015, 1.75, 2.5);
        }
        assertEquals(4, PetPerkMath.AURA_TARGETS);
        assertEquals(50, PetPerkMath.AURA_RANGE, 1e-9);
    }

    @Test
    void precursorDroneContrabandAndMiningOffCameraScaleWithLevel() {
        ConfiguredPetAbility contraband = ability(ItemType.PRECURSOR_DRONE_PET, "Contraband", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.HANDLER, contraband.getKind());
        assertTrue(contraband.isActive());
        assertValues(contraband, Rarity.LEGENDARY, 0.1, 5, 10);

        ConfiguredPetAbility offCamera = ability(ItemType.PRECURSOR_DRONE_PET, "Mining Off Camera", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, offCamera.getKind());
        assertTrue(offCamera.isActive());
        assertValues(offCamera, Rarity.LEGENDARY, 0.00005, 0.0025, 0.005);

        assertEquals(0.32, PetPerkMath.chancePerUnit(0.005, 64), 1e-9);
        assertEquals(100, PetPerkMath.chancePerUnit(10, 64), 1e-9);
        assertEquals(0, PetPerkMath.chancePerUnit(0.005, 0), 1e-9);
    }

    @Test
    void blazeBlingArmorScalesByRarityAndFusionPotatoDoublesBooks() {
        ConfiguredPetAbility bling = ability(ItemType.BLAZE_PET, "Bling Armor", Rarity.RARE);
        assertEquals(PetAbilityKind.HANDLER, bling.getKind());
        assertTrue(bling.isActive());
        assertFalse(bling.appliesAt(Rarity.UNCOMMON));
        assertValues(bling, Rarity.RARE, 0.35, 17.5, 35);
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) assertValues(ability(ItemType.BLAZE_PET, "Bling Armor", rarity), rarity, 0.4, 20, 40);

        ConfiguredPetAbility potato = ability(ItemType.BLAZE_PET, "Fusion-Style Potato", Rarity.LEGENDARY);
        assertTrue(potato.isActive());
        assertFalse(potato.appliesAt(Rarity.EPIC));
        assertEquals(1, CombatPetHooks.hotPotatoMultiplier(null), 1e-9);
        assertEquals(1, CombatPetHooks.blazeArmorMultiplier(null), 1e-9);
        assertEquals(140, PetPerkMath.boosted(100, 40), 1e-9);
    }

    @Test
    void blackCatHunterRaisesSpeedAndTheSpeedCap() {
        for (Rarity rarity : List.of(Rarity.LEGENDARY, Rarity.MYTHIC)) {
            ConfiguredPetAbility hunter = ability(ItemType.BLACK_CAT_PET, "Hunter", rarity);
            assertEquals(PetAbilityKind.STAT, hunter.getKind());
            assertTrue(hunter.isActive());
            assertValues(hunter, rarity, 1, 50, 100);
        }
        assertEquals(400, PetPerkMath.speedWithRaisedCap(500, 400, 0), 1e-9);
        assertEquals(401, PetPerkMath.speedWithRaisedCap(500, 400, 1), 1e-9);
        assertEquals(450, PetPerkMath.speedWithRaisedCap(700, 400, 50), 1e-9);
        assertEquals(500, PetPerkMath.speedWithRaisedCap(700, 400, 100), 1e-9);
        assertEquals(300, PetPerkMath.speedWithRaisedCap(300, 400, 100), 1e-9);
        ItemStatistics stats = ItemStatistics.empty();
        assertEquals(stats, CombatPetHooks.withRaisedSpeedCap(null, stats));
    }

    @Test
    void lionKingOfTheJungleScalesAndRemembersAttackersForAMinute() {
        ConfiguredPetAbility king = ability(ItemType.LION_PET, "King of the Jungle", Rarity.LEGENDARY);
        assertEquals(PetAbilityKind.TEXT, king.getKind());
        assertTrue(king.isActive());
        assertFalse(king.appliesAt(Rarity.EPIC));
        assertValues(king, Rarity.LEGENDARY, 1.5, 75, 150);

        UUID player = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        long now = 9_000_000;
        assertFalse(MobAggroLog.hasAttacked(player, attacker, now));
        MobAggroLog.record(player, attacker, now);
        assertTrue(MobAggroLog.hasAttacked(player, attacker, now + 59_999));
        assertFalse(MobAggroLog.hasAttacked(player, attacker, now + MobAggroLog.MEMORY_MS));
        assertFalse(MobAggroLog.hasAttacked(player, stranger, now + 1));
        assertFalse(MobAggroLog.hasAttacked(UUID.randomUUID(), attacker, now + 1));
        MobAggroLog.clear(player);
        assertFalse(MobAggroLog.hasAttacked(player, attacker, now + 1));
    }

    @Test
    void lionFirstPounceBoostsTheEnchantsByRarity() {
        ConfiguredPetAbility pounce = ability(ItemType.LION_PET, "First Pounce", Rarity.RARE);
        assertEquals(PetAbilityKind.TEXT, pounce.getKind());
        assertTrue(pounce.isActive());
        assertFalse(pounce.appliesAt(Rarity.UNCOMMON));
        assertValues(pounce, Rarity.RARE, 0.75, 37.5, 75);
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) assertValues(ability(ItemType.LION_PET, "First Pounce", rarity), rarity, 1, 50, 100);
        assertEquals(1, CombatPetHooks.firstPounceMultiplier(null), 1e-9);
        assertEquals(150, PetPerkMath.boosted(75, 100), 1e-9);
    }
}
