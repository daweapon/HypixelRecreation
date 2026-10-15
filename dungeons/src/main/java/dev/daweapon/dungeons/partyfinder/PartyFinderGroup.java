package dev.daweapon.dungeons.partyfinder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A Party Finder group for any activity: {@code K} is what it queues (a Catacombs floor, a Kuudra tier) and
 * {@code C} the class a member picks, if the activity has classes. An empty class set allows every class.
 */
public class PartyFinderGroup<K, C> {
    public enum JoinCheck {
        OK, CLOSED, FULL, ALREADY_MEMBER, IN_ANOTHER_GROUP, LEVEL_TOO_LOW, CLASS_NOT_ALLOWED
    }

    private final UUID id;
    private final UUID leaderId;
    private final K key;
    private final String note;
    private final int minimumLevel;
    private final Set<C> allowedClasses;
    private final int capacity;
    private final int minimumToStart;
    private final long createdAt;
    private final Map<UUID, PartyFinderMember<C>> members = new LinkedHashMap<>();
    private boolean open = true;

    public PartyFinderGroup(UUID id, PartyFinderMember<C> leader, K key, String note, int minimumLevel,
                            Set<C> allowedClasses, int capacity, int minimumToStart, long createdAt) {
        if (capacity < 1) throw new IllegalArgumentException("A Party Finder group needs room for its leader");
        this.id = id;
        this.leaderId = leader.playerId();
        this.key = key;
        this.note = note == null ? "" : note;
        this.minimumLevel = Math.max(0, minimumLevel);
        this.allowedClasses = Set.copyOf(new LinkedHashSet<>(allowedClasses));
        this.capacity = capacity;
        this.minimumToStart = minimumToStart;
        this.createdAt = createdAt;
        members.put(leader.playerId(), leader.withRole(PartyFinderRole.LEADER));
    }

    public UUID id() {
        return id;
    }

    public UUID leaderId() {
        return leaderId;
    }

    public PartyFinderMember<C> leader() {
        return members.get(leaderId);
    }

    public K key() {
        return key;
    }

    public String note() {
        return note;
    }

    public int minimumLevel() {
        return minimumLevel;
    }

    public Set<C> allowedClasses() {
        return allowedClasses;
    }

    public int capacity() {
        return capacity;
    }

    public long createdAt() {
        return createdAt;
    }

    public boolean open() {
        return open;
    }

    public List<PartyFinderMember<C>> members() {
        return List.copyOf(new ArrayList<>(members.values()));
    }

    public boolean contains(UUID playerId) {
        return members.containsKey(playerId);
    }

    public int size() {
        return members.size();
    }

    public boolean full() {
        return members.size() >= capacity;
    }

    public boolean ready() {
        return members.size() >= minimumToStart && members.values().stream().allMatch(PartyFinderMember::ready);
    }

    public boolean allows(C selectedClass) {
        return selectedClass == null || allowedClasses.isEmpty() || allowedClasses.contains(selectedClass);
    }

    public JoinCheck check(PartyFinderMember<C> member) {
        if (!open) return JoinCheck.CLOSED;
        if (members.containsKey(member.playerId())) return JoinCheck.ALREADY_MEMBER;
        if (full()) return JoinCheck.FULL;
        if (member.level() < minimumLevel) return JoinCheck.LEVEL_TOO_LOW;
        if (!allows(member.selectedClass())) return JoinCheck.CLASS_NOT_ALLOWED;
        return JoinCheck.OK;
    }

    public void join(PartyFinderMember<C> member) {
        JoinCheck check = check(member);
        if (check != JoinCheck.OK) throw new IllegalStateException("Can't join Party Finder group " + id + ": " + check);
        members.put(member.playerId(), member.withRole(PartyFinderRole.MEMBER));
    }

    /** Adds a member without the checks, for groups read back from storage. */
    public void restore(PartyFinderMember<C> member) {
        if (!member.playerId().equals(leaderId)) members.put(member.playerId(), member);
    }

    /** Removes a player; the group closes when its leader leaves. Returns whether the player was in it. */
    public boolean leave(UUID playerId) {
        if (members.remove(playerId) == null) return false;
        if (playerId.equals(leaderId)) open = false;
        return true;
    }

    public void updateClass(UUID playerId, C type) {
        if (!allows(type)) throw new IllegalArgumentException(type + " is not allowed in this group");
        members.put(playerId, member(playerId).withClass(type));
    }

    public void ready(UUID playerId, boolean ready) {
        members.put(playerId, member(playerId).withReady(ready));
    }

    public void close() {
        open = false;
    }

    public PartyFinderMember<C> member(UUID playerId) {
        PartyFinderMember<C> member = members.get(playerId);
        if (member == null) throw new IllegalArgumentException("Player is not in this Party Finder group");
        return member;
    }
}
