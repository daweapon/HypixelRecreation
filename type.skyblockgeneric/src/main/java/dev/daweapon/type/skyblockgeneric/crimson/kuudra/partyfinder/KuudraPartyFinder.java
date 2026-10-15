package dev.daweapon.type.skyblockgeneric.crimson.kuudra.partyfinder;

import dev.daweapon.dungeons.partyfinder.PartyFinderBoard;
import dev.daweapon.dungeons.partyfinder.PartyFinderGroup;
import dev.daweapon.dungeons.partyfinder.PartyFinderMember;
import dev.daweapon.dungeons.partyfinder.PartyFinderSearch;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraPartyCheck;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * The Kuudra Party Finder board shared by every SkyBlock server: groups listed per tier with a note and a
 * minimum Combat level, up to 4 players each. Built on the generic Party Finder the Catacombs use.
 */
public final class KuudraPartyFinder {
    public static final int CAPACITY = KuudraPartyCheck.MAX_PARTY_SIZE;
    public static final int MAX_COMBAT_LEVEL = 60;
    public static final int MAX_NOTE_LENGTH = 40;
    public static final long LISTING_LIFETIME_MILLIS = 30 * 60_000L;

    private static volatile KuudraPartyFinderStore store = new KuudraPartyFinderStore.Redis();
    private static volatile LongSupplier clock = System::currentTimeMillis;

    private KuudraPartyFinder() {
    }

    public static void use(KuudraPartyFinderStore newStore, LongSupplier newClock) {
        store = newStore;
        clock = newClock;
    }

    public record Listing(PartyFinderMember<Void> leader, List<PartyFinderMember<Void>> party, KuudraTier tier,
                          String note, int minimumCombat) {
    }

    /** Lists the leader's party; empty when the leader or one of the party is already in a group. */
    public static Optional<PartyFinderGroup<KuudraTier, Void>> list(Listing listing) {
        return store.update(board -> {
            prune(board);
            boolean taken = board.groupOf(listing.leader().playerId()).isPresent()
                    || listing.party().stream().anyMatch(member -> board.groupOf(member.playerId()).isPresent());
            if (taken) return Optional.empty();
            PartyFinderGroup<KuudraTier, Void> group = new PartyFinderGroup<>(UUID.randomUUID(), listing.leader(),
                    listing.tier(), cleanNote(listing.note()), clampLevel(listing.minimumCombat()), Set.of(), CAPACITY, 1,
                    clock.getAsLong());
            listing.party().stream().limit(CAPACITY - 1).forEach(group::restore);
            board.add(group);
            return Optional.of(group);
        });
    }

    /** Open groups matching the search that still have room, in the search's order. */
    public static List<PartyFinderGroup<KuudraTier, Void>> search(PartyFinderSearch<KuudraTier> search) {
        PartyFinderBoard<KuudraTier, Void> board = store.read();
        prune(board);
        return board.search(search).stream().filter(group -> !group.full()).toList();
    }

    public static Optional<PartyFinderGroup<KuudraTier, Void>> group(UUID id) {
        PartyFinderBoard<KuudraTier, Void> board = store.read();
        prune(board);
        return board.group(id);
    }

    public static Optional<PartyFinderGroup<KuudraTier, Void>> groupOf(UUID player) {
        PartyFinderBoard<KuudraTier, Void> board = store.read();
        prune(board);
        return board.groupOf(player);
    }

    public static PartyFinderGroup.JoinCheck join(UUID groupId, PartyFinderMember<Void> member) {
        return store.update(board -> {
            prune(board);
            return board.join(groupId, member);
        });
    }

    /** Takes a player out of their group; a leader leaving (or queueing) delists the whole group. */
    public static Optional<PartyFinderGroup<KuudraTier, Void>> leave(UUID player) {
        return store.update(board -> board.leave(player));
    }

    /** Keeps only the members still in the leader's party; a group whose leader is gone is delisted. */
    public static void retain(UUID groupId, Collection<UUID> party) {
        store.update(board -> {
            board.group(groupId).ifPresent(group -> {
                if (!party.contains(group.leaderId())) {
                    board.leave(group.leaderId());
                    return;
                }
                group.members().stream().map(PartyFinderMember::playerId).filter(id -> !party.contains(id))
                        .forEach(group::leave);
            });
            return null;
        });
    }

    public static String cleanNote(String note) {
        if (note == null) return "";
        String cleaned = note.replaceAll("[§<>{}\\p{Cntrl}]", "").trim();
        return cleaned.length() > MAX_NOTE_LENGTH ? cleaned.substring(0, MAX_NOTE_LENGTH) : cleaned;
    }

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(MAX_COMBAT_LEVEL, level));
    }

    private static void prune(PartyFinderBoard<KuudraTier, Void> board) {
        long now = clock.getAsLong();
        board.removeIf(group -> now - group.createdAt() > LISTING_LIFETIME_MILLIS);
    }
}
