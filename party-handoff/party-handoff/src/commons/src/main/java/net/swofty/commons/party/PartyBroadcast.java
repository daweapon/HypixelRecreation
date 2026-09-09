package net.swofty.commons.party;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PartyBroadcast.Invited.class, name = "Invited"),
        @JsonSubTypes.Type(value = PartyBroadcast.InviteExpired.class, name = "InviteExpired"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberJoined.class, name = "MemberJoined"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberLeft.class, name = "MemberLeft"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberKicked.class, name = "MemberKicked"),
        @JsonSubTypes.Type(value = PartyBroadcast.LeaderTransferred.class, name = "LeaderTransferred"),
        @JsonSubTypes.Type(value = PartyBroadcast.RoleChanged.class, name = "RoleChanged"),
        @JsonSubTypes.Type(value = PartyBroadcast.Disbanded.class, name = "Disbanded"),
        @JsonSubTypes.Type(value = PartyBroadcast.Chat.class, name = "Chat"),
        @JsonSubTypes.Type(value = PartyBroadcast.Warp.class, name = "Warp"),
        @JsonSubTypes.Type(value = PartyBroadcast.WarpOverview.class, name = "WarpOverview"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberSwitchedServer.class, name = "MemberSwitchedServer"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberDisconnected.class, name = "MemberDisconnected"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberRejoined.class, name = "MemberRejoined"),
        @JsonSubTypes.Type(value = PartyBroadcast.MemberDisconnectTimedOut.class, name = "MemberDisconnectTimedOut"),
        @JsonSubTypes.Type(value = PartyBroadcast.SizeChanged.class, name = "SizeChanged"),
        @JsonSubTypes.Type(value = PartyBroadcast.AllInviteToggled.class, name = "AllInviteToggled"),
        @JsonSubTypes.Type(value = PartyBroadcast.MuteToggled.class, name = "MuteToggled"),
        @JsonSubTypes.Type(value = PartyBroadcast.OfflineMembersKicked.class, name = "OfflineMembersKicked")
})
// Sealed set of party state-change events broadcast out to affected players, each knowing who its participants are.
public sealed interface PartyBroadcast {

    List<UUID> participants();

    record Invited(PendingParty party) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record InviteExpired(PendingParty party, UUID inviter, UUID invitee) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return List.of(inviter, invitee); }
    }

    record MemberJoined(FullParty party, UUID inviter, UUID joiner) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record MemberLeft(FullParty party, UUID leaver) implements PartyBroadcast {
        @Override
        public List<UUID> participants() {
            List<UUID> participants = new java.util.ArrayList<>(party.getParticipants());
            if (!participants.contains(leaver)) {
                participants.add(leaver);
            }
            return participants;
        }
    }

    record MemberKicked(FullParty party, UUID kicker, UUID kicked) implements PartyBroadcast {
        @Override
        public List<UUID> participants() {
            List<UUID> participants = new java.util.ArrayList<>(party.getParticipants());
            if (!participants.contains(kicked)) {
                participants.add(kicked);
            }
            return participants;
        }
    }

    record LeaderTransferred(FullParty party, UUID oldLeader, UUID newLeader) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record RoleChanged(FullParty party, UUID promoter, UUID promoted, FullParty.Role newRole) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record Disbanded(FullParty party, UUID disbander, String reason) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record Chat(FullParty party, UUID sender, String message) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record Warp(FullParty party, UUID warper) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record WarpOverview(FullParty party, UUID warper, List<UUID> warped, List<UUID> failed,
                        Map<UUID, String> failureReasons) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return List.of(warper); }
    }

    record MemberSwitchedServer(FullParty party, UUID mover) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record MemberDisconnected(FullParty party, UUID disconnectedPlayer, long timeoutSeconds) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record MemberRejoined(FullParty party, UUID rejoinedPlayer) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record MemberDisconnectTimedOut(FullParty party, UUID timedOutPlayer, boolean wasLeader) implements PartyBroadcast {
        @Override
        public List<UUID> participants() {
            List<UUID> participants = new java.util.ArrayList<>(party.getParticipants());
            if (!participants.contains(timedOutPlayer)) {
                participants.add(timedOutPlayer);
            }
            return participants;
        }
    }

    record SizeChanged(FullParty party, UUID setter, int size) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record AllInviteToggled(FullParty party, UUID setter, boolean enabled) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record MuteToggled(FullParty party, UUID setter, boolean muted) implements PartyBroadcast {
        @Override
        public List<UUID> participants() { return party.getParticipants(); }
    }

    record OfflineMembersKicked(FullParty party, UUID kicker, List<UUID> kicked) implements PartyBroadcast {
        @Override
        public List<UUID> participants() {
            List<UUID> participants = new java.util.ArrayList<>(party.getParticipants());
            for (UUID uuid : kicked) {
                if (!participants.contains(uuid)) participants.add(uuid);
            }
            return participants;
        }
    }
}
