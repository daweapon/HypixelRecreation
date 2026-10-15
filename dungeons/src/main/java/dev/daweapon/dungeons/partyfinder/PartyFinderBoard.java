package dev.daweapon.dungeons.partyfinder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/** Every open Party Finder group of one activity. A player is in at most one group. */
public class PartyFinderBoard<K, C> {
    private final Map<UUID, PartyFinderGroup<K, C>> groups = new LinkedHashMap<>();

    public PartyFinderGroup<K, C> add(PartyFinderGroup<K, C> group) {
        for (PartyFinderMember<C> member : group.members()) {
            if (groupOf(member.playerId()).isPresent()) {
                throw new IllegalStateException(member.name() + " is already in a Party Finder group");
            }
        }
        groups.put(group.id(), group);
        return group;
    }

    public Optional<PartyFinderGroup<K, C>> group(UUID id) {
        return Optional.ofNullable(groups.get(id)).filter(PartyFinderGroup::open);
    }

    public Optional<PartyFinderGroup<K, C>> groupOf(UUID playerId) {
        return groups.values().stream().filter(PartyFinderGroup::open).filter(group -> group.contains(playerId)).findFirst();
    }

    public List<PartyFinderGroup<K, C>> groups() {
        return groups.values().stream().filter(PartyFinderGroup::open).toList();
    }

    public List<PartyFinderGroup<K, C>> search(PartyFinderSearch<K> search) {
        List<PartyFinderGroup<K, C>> found = new ArrayList<>(groups.values().stream().filter(search::matches).toList());
        found.sort(search.order());
        return found;
    }

    public PartyFinderGroup.JoinCheck join(UUID groupId, PartyFinderMember<C> member) {
        PartyFinderGroup<K, C> group = groups.get(groupId);
        if (group == null || !group.open()) return PartyFinderGroup.JoinCheck.CLOSED;
        Optional<PartyFinderGroup<K, C>> current = groupOf(member.playerId());
        if (current.isPresent()) {
            return current.get().id().equals(groupId) ? PartyFinderGroup.JoinCheck.ALREADY_MEMBER : PartyFinderGroup.JoinCheck.IN_ANOTHER_GROUP;
        }
        PartyFinderGroup.JoinCheck check = group.check(member);
        if (check == PartyFinderGroup.JoinCheck.OK) group.join(member);
        return check;
    }

    /** Takes the player out of their group, and returns that group; a leader leaving delists it. */
    public Optional<PartyFinderGroup<K, C>> leave(UUID playerId) {
        Optional<PartyFinderGroup<K, C>> group = groupOf(playerId);
        group.ifPresent(found -> {
            found.leave(playerId);
            if (!found.open()) groups.remove(found.id());
        });
        return group;
    }

    public void removeIf(Predicate<PartyFinderGroup<K, C>> stale) {
        groups.values().removeIf(group -> !group.open() || stale.test(group));
    }
}
