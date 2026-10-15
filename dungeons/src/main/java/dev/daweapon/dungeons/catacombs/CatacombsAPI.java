package dev.daweapon.dungeons.catacombs;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.daweapon.dungeons.GeneratorService;
import dev.daweapon.dungeons.CatacombsDungeon;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassDefinition;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.generation.CatacombsGenerator;
import dev.daweapon.dungeons.catacombs.instance.CatacombsInstanceService;
import dev.daweapon.dungeons.catacombs.item.DungeonOrbProfile;
import dev.daweapon.dungeons.catacombs.kit.DungeonClassKit;
import dev.daweapon.dungeons.catacombs.kit.DungeonKitRegistry;
import dev.daweapon.dungeons.catacombs.map.DungeonMapRenderResult;
import dev.daweapon.dungeons.catacombs.map.DungeonMapRenderer;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRole;
import dev.daweapon.dungeons.catacombs.party.PartyFinderService;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleController;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Facade exposing Catacombs subsystems (classes, mobs, kits, puzzles, generation, runs) through static registries.
public final class CatacombsAPI {
    private static final DungeonClassRegistry CLASS_REGISTRY = DungeonClassRegistry.defaults();
    private static final CatacombsRegistry CATACOMBS_REGISTRY = CatacombsRegistry.defaults();
    private static final DungeonMobRegistry MOB_REGISTRY = DungeonMobRegistry.defaults();
    private static final DungeonKitRegistry KIT_REGISTRY = DungeonKitRegistry.defaults();
    private static final DungeonMapRenderer MAP_RENDERER = new DungeonMapRenderer();

    public static CatacombsFloorDefinition floor(CatacombsFloor floor, CatacombsMode mode) {
        return CATACOMBS_REGISTRY.floor(floor, mode);
    }

    public static DungeonClassDefinition dungeonClass(DungeonClassType type) {
        return CLASS_REGISTRY.definition(type);
    }

    public static Map<DungeonClassType, DungeonClassDefinition> classes() {
        return CLASS_REGISTRY.definitions();
    }

    public static DungeonOrbProfile dungeonOrb(DungeonClassType type) {
        return DungeonOrbProfile.forClass(dungeonClass(type));
    }

    public static List<DungeonClassKit> kits(DungeonClassType type) {
        return KIT_REGISTRY.kits(type);
    }

    public static DungeonClassKit kit(String id) {
        return KIT_REGISTRY.kit(id);
    }

    public static PartyFinderService partyFinder() {
        return new PartyFinderService();
    }

    public static CatacombsInstanceService instances() {
        return new CatacombsInstanceService();
    }

    public static List<DungeonMobDefinition> mobs(CatacombsFloor floor, DungeonMobRole role) {
        return MOB_REGISTRY.spawnable(floor, role);
    }

    public static DungeonMobDefinition mob(String id) {
        return MOB_REGISTRY.mob(id);
    }

    public static PuzzleController puzzle(CatacombsPuzzle puzzle) {
        return CatacombsPuzzleFactory.create(puzzle);
    }

    public static GeneratorService generator(CatacombsFloorDefinition definition) {
        return CatacombsGenerator.generator(definition);
    }

    public static DungeonMapRenderResult renderMap(CatacombsDungeon dungeon, Path outputPath) throws IOException {
        return MAP_RENDERER.renderPng(dungeon, outputPath);
    }

    public static CatacombsRunState startRun(CatacombsRunConfig config) {
        return CatacombsRunState.start(config);
    }
}
