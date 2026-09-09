package net.swofty.dungeons.catacombs;

import net.swofty.dungeons.catacombs.boss.CatacombsBossEncounter;
import net.swofty.dungeons.catacombs.puzzle.CatacombsPuzzle;
import net.swofty.dungeons.catacombs.run.DungeonRunRules;

import java.util.Set;

// Full configuration for one floor+mode combination: boss, run rules, room counts, and allowed puzzles.
public record CatacombsFloorDefinition(
        CatacombsFloor floor,
        CatacombsMode mode,
        CatacombsBossEncounter boss,
        DungeonRunRules rules,
        int puzzleRooms,
        int minibossRooms,
        int trapRooms,
        Set<CatacombsPuzzle> possiblePuzzles
) {
    public CatacombsFloorDefinition {
        possiblePuzzles = Set.copyOf(possiblePuzzles);
        if (!floor.supports(mode)) {
            throw new IllegalArgumentException(floor.displayName() + " does not support " + mode);
        }
    }
}
