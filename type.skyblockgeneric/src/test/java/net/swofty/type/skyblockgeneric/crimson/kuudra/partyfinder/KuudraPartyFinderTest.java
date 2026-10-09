package net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder;

import net.swofty.dungeons.catacombs.CatacombsFloor;
import net.swofty.dungeons.catacombs.CatacombsAPI;
import net.swofty.dungeons.catacombs.CatacombsMode;
import net.swofty.dungeons.catacombs.classes.DungeonClassType;
import net.swofty.dungeons.catacombs.party.PartyFinderListing;
import net.swofty.dungeons.catacombs.party.PartyFinderService;
import net.swofty.dungeons.partyfinder.PartyFinderGroup;
import net.swofty.dungeons.partyfinder.PartyFinderMember;
import net.swofty.dungeons.partyfinder.PartyFinderRole;
import net.swofty.dungeons.partyfinder.PartyFinderSearch;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraPartyFinderTest {
    private final AtomicLong now = new AtomicLong(1_000_000);

    @BeforeEach
    void freshBoard() {
        KuudraPartyFinder.use(new KuudraPartyFinderStore.Memory(), now::get);
    }

    private static PartyFinderMember<Void> player(String name, int combat) {
        return PartyFinderMember.member(UUID.randomUUID(), name, null, combat);
    }

    private PartyFinderGroup<KuudraTier, Void> list(PartyFinderMember<Void> leader, KuudraTier tier, String note, int combat,
                                                   PartyFinderMember<Void>... party) {
        now.incrementAndGet();
        return KuudraPartyFinder.list(new KuudraPartyFinder.Listing(leader, List.of(party), tier, note, combat)).orElseThrow();
    }

    private static PartyFinderSearch<KuudraTier> any() {
        return PartyFinderSearch.any(KuudraPartyFinder.MAX_COMBAT_LEVEL);
    }

    @Test
    void aListedPartyIsSeenWithItsTierNoteLevelAndMembers() {
        PartyFinderMember<Void> leader = player("Leader", 40);
        PartyFinderMember<Void> friend = player("Friend", 33);
        PartyFinderGroup<KuudraTier, Void> listed = list(leader, KuudraTier.INFERNAL, "need stun", 35, friend);

        List<PartyFinderGroup<KuudraTier, Void>> found = KuudraPartyFinder.search(any());
        assertEquals(1, found.size());
        PartyFinderGroup<KuudraTier, Void> group = found.getFirst();
        assertEquals(listed.id(), group.id());
        assertEquals(KuudraTier.INFERNAL, group.key());
        assertEquals("need stun", group.note());
        assertEquals(35, group.minimumLevel());
        assertEquals(List.of("Leader", "Friend"), group.members().stream().map(PartyFinderMember::name).toList());
        assertEquals(PartyFinderRole.LEADER, group.leader().role());
        assertEquals(4, group.capacity());
    }

    @Test
    void aPlayerCanOnlyBeInOneGroup() {
        PartyFinderMember<Void> leader = player("Leader", 10);
        list(leader, KuudraTier.BASIC, "", 0);
        assertTrue(KuudraPartyFinder.list(new KuudraPartyFinder.Listing(leader, List.of(), KuudraTier.HOT, "", 0)).isEmpty());

        PartyFinderMember<Void> other = player("Other", 10);
        PartyFinderGroup<KuudraTier, Void> second = list(other, KuudraTier.HOT, "", 0);
        assertEquals(PartyFinderGroup.JoinCheck.IN_ANOTHER_GROUP, KuudraPartyFinder.join(second.id(), leader));
    }

    @Test
    void joiningChecksTheCombatLevelAndRoom() {
        PartyFinderGroup<KuudraTier, Void> group = list(player("Leader", 50), KuudraTier.FIERY, "", 30);

        assertEquals(PartyFinderGroup.JoinCheck.LEVEL_TOO_LOW, KuudraPartyFinder.join(group.id(), player("Weak", 29)));
        PartyFinderMember<Void> a = player("A", 30);
        assertEquals(PartyFinderGroup.JoinCheck.OK, KuudraPartyFinder.join(group.id(), a));
        assertEquals(PartyFinderGroup.JoinCheck.ALREADY_MEMBER, KuudraPartyFinder.join(group.id(), a));
        assertEquals(PartyFinderGroup.JoinCheck.OK, KuudraPartyFinder.join(group.id(), player("B", 31)));
        assertEquals(PartyFinderGroup.JoinCheck.OK, KuudraPartyFinder.join(group.id(), player("C", 60)));
        assertEquals(PartyFinderGroup.JoinCheck.FULL, KuudraPartyFinder.join(group.id(), player("D", 60)));

        assertTrue(KuudraPartyFinder.search(any()).isEmpty(), "a full group is no longer listed");
        assertEquals(4, KuudraPartyFinder.groupOf(a.playerId()).orElseThrow().size());
    }

    @Test
    void theSearchFiltersByTierTextAndLevelAndSorts() {
        PartyFinderGroup<KuudraTier, Void> basic = list(player("Alpha", 5), KuudraTier.BASIC, "chill run", 0);
        PartyFinderGroup<KuudraTier, Void> infernal = list(player("Bravo", 50), KuudraTier.INFERNAL, "lf stun", 60);
        PartyFinderGroup<KuudraTier, Void> burning = list(player("Charlie", 30), KuudraTier.BURNING, "need stun", 24,
                player("Extra", 20));

        assertEquals(List.of(burning.id(), infernal.id(), basic.id()), ids(KuudraPartyFinder.search(any())));
        assertEquals(List.of(infernal.id()), ids(KuudraPartyFinder.search(any().withKey(KuudraTier.INFERNAL))));
        assertEquals(List.of(burning.id(), infernal.id()), ids(KuudraPartyFinder.search(any().withText("STUN"))));
        assertEquals(List.of(basic.id()), ids(KuudraPartyFinder.search(any().withText("alph"))));
        assertEquals(List.of(burning.id(), basic.id()), ids(KuudraPartyFinder.search(any().withLevels(0, 40))));
        assertEquals(List.of(infernal.id(), burning.id(), basic.id()),
                ids(KuudraPartyFinder.search(any().withSort(PartyFinderSearch.Sort.HIGHEST_LEVEL))));
        assertEquals(burning.id(), KuudraPartyFinder.search(any().withSort(PartyFinderSearch.Sort.LARGEST_GROUP)).getFirst().id());
    }

    @Test
    void theLeaderLeavingDelistsAndAMemberLeavingFreesTheirSpot() {
        PartyFinderMember<Void> leader = player("Leader", 30);
        PartyFinderGroup<KuudraTier, Void> group = list(leader, KuudraTier.HOT, "", 0);
        PartyFinderMember<Void> member = player("Member", 30);
        KuudraPartyFinder.join(group.id(), member);

        KuudraPartyFinder.leave(member.playerId());
        assertEquals(1, KuudraPartyFinder.group(group.id()).orElseThrow().size());

        KuudraPartyFinder.leave(leader.playerId());
        assertTrue(KuudraPartyFinder.group(group.id()).isEmpty());
        assertEquals(PartyFinderGroup.JoinCheck.CLOSED, KuudraPartyFinder.join(group.id(), player("Late", 30)));
    }

    @Test
    void membersWhoLeftTheLeadersPartyAreDropped() {
        PartyFinderMember<Void> leader = player("Leader", 30);
        PartyFinderMember<Void> stays = player("Stays", 30);
        PartyFinderMember<Void> left = player("Left", 30);
        PartyFinderGroup<KuudraTier, Void> group = list(leader, KuudraTier.HOT, "", 0, stays, left);

        KuudraPartyFinder.retain(group.id(), List.of(leader.playerId(), stays.playerId()));
        assertEquals(List.of("Leader", "Stays"), KuudraPartyFinder.group(group.id()).orElseThrow().members().stream()
                .map(PartyFinderMember::name).toList());

        KuudraPartyFinder.retain(group.id(), List.of(stays.playerId()));
        assertTrue(KuudraPartyFinder.group(group.id()).isEmpty(), "a group whose leader left the party is delisted");
    }

    @Test
    void listingsExpire() {
        PartyFinderGroup<KuudraTier, Void> group = list(player("Leader", 30), KuudraTier.HOT, "", 0);
        now.addAndGet(KuudraPartyFinder.LISTING_LIFETIME_MILLIS);
        assertTrue(KuudraPartyFinder.group(group.id()).isPresent());
        now.addAndGet(1000);
        assertTrue(KuudraPartyFinder.group(group.id()).isEmpty());
        assertTrue(KuudraPartyFinder.search(any()).isEmpty());
    }

    @Test
    void notesAndLevelsAreCleaned() {
        assertEquals("need stun", KuudraPartyFinder.cleanNote("  need §stun "));
        assertEquals("bold", KuudraPartyFinder.cleanNote("<bold>"));
        assertEquals(KuudraPartyFinder.MAX_NOTE_LENGTH, KuudraPartyFinder.cleanNote("x".repeat(80)).length());
        assertEquals(60, KuudraPartyFinder.clampLevel(99));
        assertEquals(0, KuudraPartyFinder.clampLevel(-3));
        PartyFinderGroup<KuudraTier, Void> group = list(player("Leader", 30), KuudraTier.HOT, "{}<x>", 75);
        assertEquals("x", group.note());
        assertEquals(60, group.minimumLevel());
    }

    @Test
    void theCatacombsPartyFinderStillWorksOnTheSharedCore() {
        PartyFinderService service = CatacombsAPI.partyFinder();
        PartyFinderMember<DungeonClassType> leader = PartyFinderMember.leader(UUID.randomUUID(), "Tank", DungeonClassType.TANK, 20);
        PartyFinderListing listing = service.create(leader, CatacombsAPI.floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL),
                10, Set.of(DungeonClassType.TANK, DungeonClassType.HEALER));

        assertEquals(CatacombsFloor.FLOOR_ONE, listing.floor());
        assertEquals(PartyFinderListing.CAPACITY, listing.capacity());
        assertEquals(PartyFinderGroup.JoinCheck.LEVEL_TOO_LOW,
                service.join(listing.id(), PartyFinderMember.member(UUID.randomUUID(), "Low", DungeonClassType.HEALER, 9)));
        {
            assertEquals(PartyFinderGroup.JoinCheck.CLASS_NOT_ALLOWED,
                    service.join(listing.id(), PartyFinderMember.member(UUID.randomUUID(), "Mage", DungeonClassType.MAGE, 30)));
        }
        PartyFinderMember<DungeonClassType> healer = PartyFinderMember.member(UUID.randomUUID(), "Healer", DungeonClassType.HEALER, 30);
        assertEquals(PartyFinderGroup.JoinCheck.OK, service.join(listing.id(), healer));
        assertFalse(listing.ready());
        listing.ready(leader.playerId(), true);
        listing.ready(healer.playerId(), true);
        assertTrue(listing.ready());
        assertEquals(1, service.openListings().size());
    }

    private static List<UUID> ids(List<PartyFinderGroup<KuudraTier, Void>> groups) {
        return groups.stream().map(PartyFinderGroup::id).toList();
    }
}
