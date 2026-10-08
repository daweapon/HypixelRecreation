package net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature;

import net.swofty.type.skyblockgeneric.fishing.lava.LavaSeaCreature;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.SeaCreatureDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeaCreatureSpawnerTest {

    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    @Test
    void everyConfiguredSeaCreatureIsSpawnable() {
        for (SeaCreatureDefinition definition : FishingRegistry.getSeaCreatures()) {
            assertTrue(SeaCreatureSpawner.canSpawn(definition.id()), definition.id() + " cannot be spawned");
        }
    }

    @Test
    void everyLavaSeaCreatureIsSpawnable() {
        for (LavaSeaCreature creature : LavaSeaCreature.values()) {
            assertTrue(SeaCreatureSpawner.canSpawn(creature.name()), creature + " cannot be spawned");
        }
    }

    @Test
    void resolvesIdsDisplayNamesAndLooseSpellings() {
        assertEquals("LORD_JAWBUS", SeaCreatureSpawner.resolveId("lord_jawbus"));
        assertEquals("LORD_JAWBUS", SeaCreatureSpawner.resolveId("LordJawbus"));
        assertEquals("FIERY_SCUTTLER", SeaCreatureSpawner.resolveId("fiery-scuttler"));
        assertEquals("PLHLEGBLAST", SeaCreatureSpawner.resolveId("Plhlegblast"));
        assertNull(SeaCreatureSpawner.resolveId("NOT_A_SEA_CREATURE"));
    }
}
