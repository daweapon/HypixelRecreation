package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.greatspook.mob.DeadlineMob;
import dev.daweapon.type.skyblockgeneric.greatspook.mob.PrimalProfiles;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimalFearTest {
    @Test
    void cooldownDropsThreeSecondsPerFearWithFifteenSecondFloor() {
        assertEquals(360_000, PrimalFearRules.cooldownMillis(0));
        assertEquals(180_000, PrimalFearRules.cooldownMillis(60));
        assertEquals(15_000, PrimalFearRules.cooldownMillis(120));
        assertEquals(15_000, PrimalFearRules.cooldownMillis(500));
    }

    @Test
    void cooldownFollowsCurrentFearInstantly() {
        long spawned = 1_000_000;
        assertEquals(360_000 - 10_000, PrimalFearRules.remainingMillis(spawned, 0, spawned + 10_000));
        assertEquals(170_000, PrimalFearRules.remainingMillis(spawned, 60, spawned + 10_000));
        assertEquals(0, PrimalFearRules.remainingMillis(spawned, 120, spawned + 15_000));
    }

    @Test
    void chanceGrowsWithFearButIsCapped() {
        assertEquals(10, PrimalFearRules.chancePercent(0), 1e-9);
        assertTrue(PrimalFearRules.chancePercent(100) > PrimalFearRules.chancePercent(10));
        assertEquals(PrimalFearRules.MAXIMUM_CHANCE_PERCENT, PrimalFearRules.chancePercent(100_000), 1e-9);
    }

    @Test
    void hitsRemoveOneHealthPointUnlessFearamaScalesThem() {
        assertEquals(1f, PrimalFearRules.hitDamage(80, false), 1e-6);
        assertEquals(1.8f, PrimalFearRules.hitDamage(80, true), 1e-6);
        assertEquals(5, PrimalFearRules.skulls(5));
        assertEquals(2, PrimalFearRules.skulls(1.4));
        assertEquals(0, PrimalFearRules.skulls(0));
    }

    @Test
    void fearReducesDamageTakenOnePercentPerPointUpToImmunity() {
        assertEquals(1, PrimalFearRules.damageTakenMultiplier(0), 1e-9);
        assertEquals(0.29, PrimalFearRules.damageTakenMultiplier(71), 1e-9);
        assertEquals(0, PrimalFearRules.damageTakenMultiplier(150), 1e-9);
    }

    @Test
    void fearcreasingGivesPerTenKillsUpToTheCap() {
        assertEquals(0, PrimalFearRules.creasingFear(0.4, 9), 1e-9);
        assertEquals(0.4, PrimalFearRules.creasingFear(0.4, 10), 1e-9);
        assertEquals(6, PrimalFearRules.creasingFear(0.4, 150), 1e-9);
        assertEquals(6, PrimalFearRules.creasingFear(0.4, 492), 1e-9);
    }

    @Test
    void deadlineCountdownMatchesCapture() {
        assertEquals(5, DeadlineMob.secondsLeft(0, 0));
        assertEquals(4, DeadlineMob.secondsLeft(0, 1_000));
        assertEquals(1, DeadlineMob.secondsLeft(0, 4_999));
        assertEquals(0, DeadlineMob.secondsLeft(0, 5_000));
        assertFalse(DeadlineMob.moved(new Pos(1, 2, 3), new Pos(1.02, 2, 3)));
        assertTrue(DeadlineMob.moved(new Pos(1, 2, 3), new Pos(1.5, 2, 3)));
    }

    @Test
    void mathProblemsAreWholeNumberMultiplications() {
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            MathProblem problem = MathProblem.random(random);
            assertTrue(problem.accepts(String.valueOf(problem.answer())));
            assertTrue(problem.accepts(" " + problem.answer() + " "));
            assertFalse(problem.accepts(String.valueOf(problem.answer() + 1)));
            assertFalse(problem.accepts("nope"));
            assertEquals(3, problem.text().split(" ").length);
        }
    }

    @Test
    void everyPrimalMobSharesTheWikiProfile() {
        for (Manifestation kind : Manifestation.ALL) {
            CrimsonMobProfile profile = PrimalProfiles.of(kind.mobId(), kind.displayName());
            assertEquals(29, profile.level());
            assertEquals(5, profile.health());
            assertEquals(1, profile.damage());
            assertEquals(List.of(MobType.SPOOKY, MobType.SHIELDED), profile.mobTypes());
            assertEquals(43, profile.combatXp());
            assertEquals(9, profile.coins());
            assertEquals(31, profile.orbs());
            assertFalse(profile.corruptible());
        }
    }

    @Test
    void dropTableMatchesTheWiki() {
        List<MobDrop> drops = PrimalProfiles.drops();
        assertEquals(4, drops.size());
        assertEquals(100, drops.get(0).chancePercent());
        assertEquals(List.of("DARK_CANDY"), drops.get(0).choices());
        assertEquals(100, drops.get(1).chancePercent());
        assertEquals(50, drops.get(2).chancePercent());
        assertEquals(List.of("GREEN_CANDY"), drops.get(2).choices());
        assertEquals(PrimalProfiles.PURPLE_CHANCE_PERCENT, drops.get(3).chancePercent());
        drops.forEach(drop -> assertFalse(drop.magicFind()));
    }

    @Test
    void manifestationIdsMatchTheWiki() {
        assertEquals(List.of("DEADLINE_100", "DARKNESS_SHADE_100", "COMMITMENT_PHOBIA", "MATH_TEACHER_100",
                "PUBLIC_SPEAKING_DEMON_100", "VEGAN_CRAWLER_100"),
                Manifestation.ALL.stream().map(Manifestation::mobId).toList());
        assertEquals(14, PrimalFearDialogue.DEADLINE.size());
        assertEquals(11, PrimalFearDialogue.DARKNESS_SHADE.size());
        assertEquals(10, PrimalFearDialogue.COMMITMENT_SIGNED.size());
        assertEquals(7, PrimalFearDialogue.DOCUMENTS.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void eerieAbilitiesAreWiredAndDarkCandyIsObtainable() throws Exception {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(Path.of("../configuration/skyblock/items"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
        PetComponent eerie = new SkyBlockItem(ItemType.EERIE_PET).getComponent(PetComponent.class);
        for (String name : List.of("Fearnesy", "Fearama", "Fearcreasing")) {
            ConfiguredPetAbility ability = eerie.getAbilities().stream()
                    .filter(candidate -> candidate.getName().equals(name)).findFirst().orElseThrow();
            assertTrue(ability.isActive(), name);
        }
        assertEquals(0.4, eerieAbility(eerie, "Fearcreasing").valuesAt(dev.daweapon.commons.skyblock.item.Rarity.LEGENDARY, 100)[0], 1e-9);
        assertTrue(ItemType.get("DARK_CANDY") != null);
    }

    private static ConfiguredPetAbility eerieAbility(PetComponent pet, String name) {
        return pet.getAbilities().stream().filter(candidate -> candidate.getName().equals(name)).findFirst().orElseThrow();
    }
}
