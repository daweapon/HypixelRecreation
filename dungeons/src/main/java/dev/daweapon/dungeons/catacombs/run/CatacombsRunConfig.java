package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;

import java.util.Map;
import java.util.UUID;

public record CatacombsRunConfig(
        CatacombsFloorDefinition floor,
        Map<UUID, DungeonClassType> partyClasses,
        int totalRooms,
        int totalSecrets,
        boolean soloAllowed
) {
    public CatacombsRunConfig(CatacombsFloorDefinition floor, Map<UUID, DungeonClassType> partyClasses,
                              int totalRooms, int totalSecrets) {
        this(floor, partyClasses, totalRooms, totalSecrets, false);
    }

    public CatacombsRunConfig {
        partyClasses = Map.copyOf(partyClasses);
        if (!floor.rules().allowsPartySize(partyClasses.size(), soloAllowed)) {
            throw new IllegalArgumentException("Invalid Catacombs party size " + partyClasses.size());
        }
    }
}
