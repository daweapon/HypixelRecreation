package net.swofty.type.skyblockgeneric.user.island;

import net.hollowcube.polar.PolarWorld;
import org.tinylog.Logger;

import java.util.Map;

/**
 * The ordered chain of island world upgrades, keyed by the version each step upgrades from.
 *
 * Bumping {@code HypixelConst.currentIslandVersion} without registering the step that reaches it
 * would let existing islands skip whatever the bump was for, so startup refuses that state.
 */
public final class IslandMigrations {
    private static final Map<Integer, IslandMigration> STEPS = Map.of();

    private IslandMigrations() {
    }

    public static void validate(int currentVersion) {
        validate(STEPS, currentVersion);
    }

    static void validate(Map<Integer, IslandMigration> steps, int currentVersion) {
        for (int version = 1; version < currentVersion; version++) {
            if (!steps.containsKey(version)) {
                throw new IllegalStateException("Island version is " + currentVersion
                        + " but no migration upgrades version " + version + " to " + (version + 1));
            }
        }
    }

    public static PolarWorld apply(PolarWorld world, SkyBlockIsland island, int fromVersion, int toVersion) {
        return apply(STEPS, world, island, fromVersion, toVersion);
    }

    static PolarWorld apply(Map<Integer, IslandMigration> steps, PolarWorld world, SkyBlockIsland island,
                            int fromVersion, int toVersion) {
        PolarWorld current = world;
        for (int version = Math.max(fromVersion, 1); version < toVersion; version++) {
            IslandMigration step = steps.get(version);
            if (step == null) {
                throw new IllegalStateException("No migration upgrades island version " + version);
            }
            current = step.migrate(current, island);
            Logger.info("Migrated island world from version {} to {}", version, version + 1);
        }
        return current;
    }
}
