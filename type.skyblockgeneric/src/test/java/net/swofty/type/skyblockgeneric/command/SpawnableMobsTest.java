package net.swofty.type.skyblockgeneric.command;

import net.minestom.server.MinecraftServer;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.SpawnableMobs;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureSpawner;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnableMobsTest {
    @BeforeAll
    static void registerMobs() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.skyblockgeneric.entity.mob.mobs", SkyBlockMob.class)
                .forEach(mob -> MobRegistry.registerExtraMob(mob.getClass()));
    }

    @Test
    void everySeaCreatureIsSpawnable() {
        SeaCreatureSpawner.ids().forEach(id -> assertNotNull(SpawnableMobs.find(id), id + " cannot be spawned"));
    }

    @Test
    void everyRegisteredMobIsSpawnable() {
        assertTrue(MobRegistry.REGISTERED_MOBS.size() > 50);
        long spawnable = SpawnableMobs.all().size();
        assertTrue(spawnable >= MobRegistry.REGISTERED_MOBS.size() - SpawnableMobs.duplicates().size(),
                "Registered mobs are missing from /spawn");
    }

    @Test
    void everyMythologicalCreatureIsSpawnable() {
        for (MythologicalCreature creature : MythologicalCreature.values()) {
            if (creature.allVariants().isEmpty()) continue;
            assertNotNull(SpawnableMobs.find(creature.name()), creature + " cannot be spawned");
        }
    }

    @Test
    void idsAreMatchedLooselyAndNeverCollide() {
        assertNotNull(SpawnableMobs.find("lord-jawbus"));
        assertEquals(SpawnableMobs.find("LORD_JAWBUS").id(), SpawnableMobs.find("lordjawbus").id());
        assertEquals(java.util.List.of(), SpawnableMobs.duplicates().stream().map(SpawnableMobs.Entry::id).toList());
    }
}
