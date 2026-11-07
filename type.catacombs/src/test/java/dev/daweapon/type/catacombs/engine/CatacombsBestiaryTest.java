package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Dungeon mob kills land in Hypixel's Catacombs Bestiary families. */
class CatacombsBestiaryTest {
    /** Mobs Hypixel's Catacombs Bestiary has no family for (NEU constants/bestiary.json). */
    private static final Set<String> NOT_IN_BESTIARY = Set.of("CRYPT_UNDEAD", "PUZZLE_BLAZE", "DEATHMITE", "BONZO_UNDEAD",
            "BONZO", "SCARF", "PROFESSOR", "THORN", "LIVID", "SADAN", "MAXOR", "STORM", "GOLDOR", "NECRON", "WITHER_KING");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static CatacombsMob spawnable(String id) {
        return new CatacombsMob(DungeonMobRegistry.defaults().mob(id), 0, false, mob -> {
        });
    }

    @Test
    void everyDungeonMobEitherHasAFamilyOrIsKnownToHaveNone() {
        for (DungeonMobDefinition definition : DungeonMobRegistry.defaults().mobs().values()) {
            boolean family = BestiaryCategory.CATACOMBS.forDungeonMob(definition.id()).isPresent();
            assertTrue(family != NOT_IN_BESTIARY.contains(definition.id()),
                    definition.id() + " needs a Catacombs Bestiary family or a place on the not-in-Bestiary list");
        }
    }

    @Test
    void aRoomMobKillCountsForItsFamily() {
        CatacombsMob grunt = spawnable("ZOMBIE_GRUNT");
        assertTrue(grunt.countsForBestiary());
        assertEquals("CATACOMBS_ZOMBIE_GRUNT", grunt.bestiaryKillTarget().getMobID());

        CatacombsMob frozen = spawnable("FROZEN_ADVENTURER");
        assertEquals("CATACOMBS_LOST_ADVENTURER", frozen.bestiaryKillTarget().getMobID());
    }

    @Test
    void theWatchersSummonsAreTheUndeadFamily() {
        assertEquals("CATACOMBS_UNDEAD", spawnable("WATCHER_UNDEAD").bestiaryKillTarget().getMobID());
    }

    @Test
    void bossesDoNotCount() {
        assertFalse(spawnable("BONZO").countsForBestiary());
    }
}
