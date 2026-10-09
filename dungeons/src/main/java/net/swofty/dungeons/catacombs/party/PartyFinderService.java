package net.swofty.dungeons.catacombs.party;

import net.swofty.dungeons.catacombs.CatacombsFloor;
import net.swofty.dungeons.catacombs.CatacombsFloorDefinition;
import net.swofty.dungeons.catacombs.classes.DungeonClassType;
import net.swofty.dungeons.partyfinder.PartyFinderBoard;
import net.swofty.dungeons.partyfinder.PartyFinderGroup;
import net.swofty.dungeons.partyfinder.PartyFinderMember;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PartyFinderService {
    private final PartyFinderBoard<CatacombsFloor, DungeonClassType> board = new PartyFinderBoard<>();

    public PartyFinderListing create(PartyFinderMember<DungeonClassType> leader, CatacombsFloorDefinition floor,
                                     int minimumCatacombsLevel, Set<DungeonClassType> allowedClasses) {
        return create(leader, floor, "", minimumCatacombsLevel, allowedClasses);
    }

    public PartyFinderListing create(PartyFinderMember<DungeonClassType> leader, CatacombsFloorDefinition floor, String note,
                                     int minimumCatacombsLevel, Set<DungeonClassType> allowedClasses) {
        PartyFinderListing listing = new PartyFinderListing(UUID.randomUUID(), leader, floor.floor(), floor.mode(), note,
                minimumCatacombsLevel, allowedClasses);
        board.add(listing);
        return listing;
    }

    public PartyFinderListing listing(UUID id) {
        return board.group(id).map(PartyFinderListing.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unknown party finder listing " + id));
    }

    public PartyFinderGroup.JoinCheck join(UUID id, PartyFinderMember<DungeonClassType> member) {
        return board.join(id, member);
    }

    public Map<UUID, PartyFinderListing> openListings() {
        Map<UUID, PartyFinderListing> open = new LinkedHashMap<>();
        board.groups().forEach(group -> open.put(group.id(), (PartyFinderListing) group));
        return Map.copyOf(open);
    }
}
