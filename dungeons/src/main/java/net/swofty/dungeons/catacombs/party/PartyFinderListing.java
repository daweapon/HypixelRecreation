package net.swofty.dungeons.catacombs.party;

import net.swofty.dungeons.catacombs.CatacombsFloor;
import net.swofty.dungeons.catacombs.CatacombsMode;
import net.swofty.dungeons.catacombs.classes.DungeonClassType;
import net.swofty.dungeons.partyfinder.PartyFinderGroup;
import net.swofty.dungeons.partyfinder.PartyFinderMember;

import java.util.Set;
import java.util.UUID;

/** A Catacombs Party Finder group: up to 5 players, at least 2 to start, Catacombs level and class checks. */
public final class PartyFinderListing extends PartyFinderGroup<CatacombsFloor, DungeonClassType> {
    public static final int CAPACITY = 5;
    public static final int MINIMUM_TO_START = 2;

    private final CatacombsMode mode;

    public PartyFinderListing(UUID id, PartyFinderMember<DungeonClassType> leader, CatacombsFloor floor, CatacombsMode mode,
                              String note, int minimumCatacombsLevel, Set<DungeonClassType> allowedClasses) {
        super(id, leader, floor, note, minimumCatacombsLevel, allowedClasses, CAPACITY, MINIMUM_TO_START,
                System.currentTimeMillis());
        this.mode = mode;
    }

    public CatacombsFloor floor() {
        return key();
    }

    public CatacombsMode mode() {
        return mode;
    }

    public int minimumCatacombsLevel() {
        return minimumLevel();
    }
}
