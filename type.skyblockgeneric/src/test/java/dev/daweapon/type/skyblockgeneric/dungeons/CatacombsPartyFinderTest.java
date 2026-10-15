package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.partyfinder.PartyFinderGroup;
import dev.daweapon.dungeons.partyfinder.PartyFinderMember;
import dev.daweapon.dungeons.partyfinder.PartyFinderRole;
import dev.daweapon.dungeons.partyfinder.PartyFinderSearch;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinder;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinderStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsPartyFinderTest {
    private final AtomicLong now = new AtomicLong(1_000_000);

    @BeforeEach
    void freshBoard() {
        CatacombsPartyFinder.use(new CatacombsPartyFinderStore.Memory(), now::get);
    }

    private static PartyFinderMember<Void> player(String name, int level) {
        return PartyFinderMember.member(UUID.randomUUID(), name, null, level);
    }

    @SafeVarargs
    private PartyFinderGroup<CatacombsFloor, Void> list(PartyFinderMember<Void> leader, CatacombsFloor floor, String note, int level,
                                                       PartyFinderMember<Void>... party) {
        now.incrementAndGet();
        return CatacombsPartyFinder.list(new CatacombsPartyFinder.Listing(leader, List.of(party), floor, note, level)).orElseThrow();
    }

    private static PartyFinderSearch<CatacombsFloor> any() {
        return PartyFinderSearch.any(CatacombsPartyFinder.MAX_CATACOMBS_LEVEL);
    }

    @Test
    void aListedPartySurvivesTheStorageCodec() {
        PartyFinderGroup<CatacombsFloor, Void> listed = list(player("Leader", 20), CatacombsFloor.FLOOR_ONE, "chill run", 1, player("Friend", 5));

        PartyFinderGroup<CatacombsFloor, Void> group = CatacombsPartyFinder.search(any()).getFirst();
        assertEquals(listed.id(), group.id());
        assertEquals(CatacombsFloor.FLOOR_ONE, group.key());
        assertEquals("chill run", group.note());
        assertEquals(1, group.minimumLevel());
        assertEquals(5, group.capacity());
        assertEquals(List.of("Leader", "Friend"), group.members().stream().map(PartyFinderMember::name).toList());
        assertEquals(PartyFinderRole.LEADER, group.leader().role());
        assertEquals(20, group.leader().level());
    }

    @Test
    void joiningChecksTheCatacombsLevelAndFillsAtFive() {
        PartyFinderGroup<CatacombsFloor, Void> group = list(player("Leader", 30), CatacombsFloor.FLOOR_ONE, "", 10);

        assertEquals(PartyFinderGroup.JoinCheck.LEVEL_TOO_LOW, CatacombsPartyFinder.join(group.id(), player("Weak", 9)));
        for (int i = 0; i < 4; i++) {
            assertEquals(PartyFinderGroup.JoinCheck.OK, CatacombsPartyFinder.join(group.id(), player("P" + i, 10)));
        }
        assertEquals(PartyFinderGroup.JoinCheck.FULL, CatacombsPartyFinder.join(group.id(), player("Late", 50)));
        assertTrue(CatacombsPartyFinder.search(any()).isEmpty(), "a full group is no longer listed");
    }

    @Test
    void theSearchFiltersByFloorAndText() {
        PartyFinderGroup<CatacombsFloor, Void> entrance = list(player("Alpha", 0), CatacombsFloor.ENTRANCE, "learning", 0);
        PartyFinderGroup<CatacombsFloor, Void> one = list(player("Bravo", 3), CatacombsFloor.FLOOR_ONE, "speed", 1);

        assertEquals(List.of(one.id(), entrance.id()), CatacombsPartyFinder.search(any()).stream().map(PartyFinderGroup::id).toList());
        assertEquals(List.of(one.id()), CatacombsPartyFinder.search(any().withKey(CatacombsFloor.FLOOR_ONE)).stream().map(PartyFinderGroup::id).toList());
        assertEquals(List.of(entrance.id()), CatacombsPartyFinder.search(any().withText("LEARN")).stream().map(PartyFinderGroup::id).toList());
    }

    @Test
    void aLeaderLeavingDelistsAndListingsExpire() {
        PartyFinderMember<Void> leader = player("Leader", 5);
        PartyFinderGroup<CatacombsFloor, Void> group = list(leader, CatacombsFloor.ENTRANCE, "", 0);
        CatacombsPartyFinder.leave(leader.playerId());
        assertTrue(CatacombsPartyFinder.group(group.id()).isEmpty());

        list(player("Other", 5), CatacombsFloor.ENTRANCE, "", 0);
        now.addAndGet(CatacombsPartyFinder.LISTING_LIFETIME_MILLIS + 1);
        assertTrue(CatacombsPartyFinder.search(any()).isEmpty());
    }

    @Test
    void notesAreCleanedAndLevelsClamped() {
        assertEquals("bhi there", CatacombsPartyFinder.cleanNote("  <b>hi{} there§  "));
        assertEquals(CatacombsPartyFinder.MAX_NOTE_LENGTH, CatacombsPartyFinder.cleanNote("x".repeat(100)).length());
        assertEquals(50, CatacombsPartyFinder.clampLevel(99));
        assertEquals(0, CatacombsPartyFinder.clampLevel(-3));
    }
}
