package dev.daweapon.commons.party;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import dev.daweapon.commons.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
// A fully-formed party with its roster of members and their roles (leader/moderator/member).
public final class FullParty implements Party {
    // Real Hypixel has no fixed party size cap - the leader can set it anywhere from 2 to 100.
    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 100;
    public static final int DEFAULT_SIZE = 20;

    private final UUID uuid;
    private final List<Member> members;
    @Setter
    private int maxSize;
    @Setter
    private boolean allowAllInvite;
    @Setter
    private boolean muted;

    @JsonCreator
    public FullParty(
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("members") List<Member> members,
            @JsonProperty("maxSize") Integer maxSize,
            @JsonProperty("allowAllInvite") boolean allowAllInvite,
            @JsonProperty("muted") boolean muted) {
        this.uuid = uuid;
        this.members = members;
        this.maxSize = maxSize != null ? maxSize : DEFAULT_SIZE;
        this.allowAllInvite = allowAllInvite;
        this.muted = muted;
    }

    public Member getFromUuid(UUID uuid) {
        return members.stream().filter(member -> member.getUuid().equals(uuid)).findFirst().orElseThrow();
    }

    public Member getLeader() {
        return members.stream().filter(member -> member.getRole() == Role.LEADER).findFirst().orElseThrow();
    }

    public static FullParty create(UUID leader, UUID invitee) {
        Member leaderMember = new Member(leader, Role.LEADER, true);
        Member inviteeMember = new Member(invitee, Role.MEMBER, true);

        return new FullParty(UUID.randomUUID(), new ArrayList<>(List.of(leaderMember, inviteeMember)), DEFAULT_SIZE, false, false);
    }

    @Override
    public List<UUID> getParticipants() {
        return members.stream().map(Member::getUuid).toList();
    }

    @Setter
    @Getter
    public static class Member {
        private final UUID uuid;
        private Role role;
        private boolean joined;

        @JsonCreator
        public Member(
                @JsonProperty("uuid") UUID uuid,
                @JsonProperty("role") Role role,
                @JsonProperty("joined") boolean joined) {
            this.uuid = uuid;
            this.role = role;
            this.joined = joined;
        }
    }

    public enum Role {
        LEADER,
        MODERATOR,
        MEMBER,
        ;

        @Override
        public String toString() {
            return StringUtility.toNormalCase(name());
        }
    }
}
