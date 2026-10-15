package dev.daweapon.dungeons.partyfinder;

import java.util.UUID;

/**
 * One player in a Party Finder group. {@code level} is the level the group's requirement is checked against
 * (Catacombs for dungeons, Combat for Kuudra); {@code selectedClass} is null where the activity has no classes.
 */
public record PartyFinderMember<C>(
        UUID playerId,
        String name,
        PartyFinderRole role,
        C selectedClass,
        int level,
        boolean ready
) {
    public static <C> PartyFinderMember<C> leader(UUID playerId, String name, C selectedClass, int level) {
        return new PartyFinderMember<>(playerId, name, PartyFinderRole.LEADER, selectedClass, level, false);
    }

    public static <C> PartyFinderMember<C> member(UUID playerId, String name, C selectedClass, int level) {
        return new PartyFinderMember<>(playerId, name, PartyFinderRole.MEMBER, selectedClass, level, false);
    }

    public PartyFinderMember<C> withClass(C selectedClass) {
        return new PartyFinderMember<>(playerId, name, role, selectedClass, level, ready);
    }

    public PartyFinderMember<C> withReady(boolean ready) {
        return new PartyFinderMember<>(playerId, name, role, selectedClass, level, ready);
    }

    public PartyFinderMember<C> withRole(PartyFinderRole role) {
        return new PartyFinderMember<>(playerId, name, role, selectedClass, level, ready);
    }
}
