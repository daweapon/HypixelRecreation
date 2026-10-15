package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.run.DungeonRunRules;

import java.util.List;
import java.util.UUID;

public final class CatacombsRoster {
    private CatacombsRoster() {
    }

    public static List<UUID> validated(List<UUID> decoded) {
        if (decoded.isEmpty()) throw new IllegalArgumentException("No party was given for the Catacombs run");
        if (decoded.size() > DungeonRunRules.MAXIMUM_PARTY_SIZE) {
            throw new IllegalArgumentException("A Catacombs party has at most " + DungeonRunRules.MAXIMUM_PARTY_SIZE + " players");
        }
        return decoded;
    }
}
