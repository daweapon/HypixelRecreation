package net.swofty.service.party;

import net.swofty.commons.party.FullParty;
import net.swofty.commons.party.PartyAction;
import net.swofty.commons.party.PartyBroadcast;
import net.swofty.commons.party.PendingParty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartyCacheTest {
    private final List<PartyBroadcast> broadcasts = new ArrayList<>();
    private final List<UUID> errorTargets = new ArrayList<>();
    private final UUID leader = UUID.randomUUID();
    private final UUID member = UUID.randomUUID();
    private final UUID third = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PartyCache.resetState();
        PartyCache.broadcaster = broadcasts::add;
        PartyCache.errorSender = (uuid, markup, args) -> errorTargets.add(uuid);
    }

    @AfterEach
    void tearDown() {
        PartyCache.resetState();
    }

    private FullParty formParty() {
        FullParty party = FullParty.create(leader, member);
        PartyCache.registerParty(party);
        return party;
    }

    private void inviteAndAccept(UUID inviter, UUID invitee) {
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(invitee, inviter)));
        PartyCache.handleAcceptInvite(new PartyAction.AcceptInvite(invitee, inviter));
    }

    @Test
    void inviteStoresPendingInviteAndBroadcasts() {
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(member, leader)));
        assertTrue(PartyCache.hasPendingInvite(member));
        assertInstanceOf(PartyBroadcast.Invited.class, broadcasts.getLast());
    }

    @Test
    void acceptingInviteCreatesPartyWithLeaderAndMember() {
        inviteAndAccept(leader, member);
        FullParty party = PartyCache.getPartyFromPlayer(leader);
        assertNotNull(party);
        assertSame(party, PartyCache.getPartyFromPlayer(member));
        assertEquals(FullParty.Role.LEADER, party.getFromUuid(leader).getRole());
        assertEquals(FullParty.Role.MEMBER, party.getFromUuid(member).getRole());
        assertFalse(PartyCache.hasPendingInvite(member));
        assertInstanceOf(PartyBroadcast.MemberJoined.class, broadcasts.getLast());
    }

    @Test
    void acceptingWithoutInviteReportsError() {
        PartyCache.handleAcceptInvite(new PartyAction.AcceptInvite(member, leader));
        assertEquals(List.of(member), errorTargets);
        assertFalse(PartyCache.isInParty(member));
    }

    @Test
    void acceptingInviteFromWrongInviterIsRejected() {
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(member, leader)));
        PartyCache.handleAcceptInvite(new PartyAction.AcceptInvite(member, third));
        assertEquals(List.of(member), errorTargets);
        assertFalse(PartyCache.isInParty(member));
    }

    @Test
    void plainMemberCannotInviteUnlessAllInviteEnabled() {
        FullParty party = formParty();
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(third, member)));
        assertEquals(List.of(member), errorTargets);
        assertFalse(PartyCache.hasPendingInvite(third));

        party.setAllowAllInvite(true);
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(third, member)));
        assertTrue(PartyCache.hasPendingInvite(third));
    }

    @Test
    void inviteRejectedWhenPartyFull() {
        FullParty party = formParty();
        party.setMaxSize(2);
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(third, leader)));
        assertEquals(List.of(leader), errorTargets);
        assertFalse(PartyCache.hasPendingInvite(third));
    }

    @Test
    void acceptRejectedWhenPartyBecameFull() {
        FullParty party = formParty();
        PartyCache.handleInvite(new PartyAction.Invite(PendingParty.create(third, leader)));
        party.setMaxSize(2);
        PartyCache.handleAcceptInvite(new PartyAction.AcceptInvite(third, leader));
        assertEquals(List.of(third), errorTargets);
        assertFalse(PartyCache.isInParty(third));
    }

    @Test
    void memberLeavingRemovesOnlyThem() {
        FullParty party = formParty();
        PartyCache.handleLeave(new PartyAction.Leave(member));
        assertFalse(PartyCache.isInParty(member));
        assertTrue(PartyCache.isInParty(leader));
        assertEquals(1, party.getMembers().size());
        assertInstanceOf(PartyBroadcast.MemberLeft.class, broadcasts.getLast());
    }

    @Test
    void leaderLeavingDisbandsParty() {
        FullParty party = formParty();
        PartyCache.handleLeave(new PartyAction.Leave(leader));
        assertFalse(PartyCache.isInParty(leader));
        assertFalse(PartyCache.isInParty(member));
        assertFalse(PartyCache.isPartyActive(party.getUuid()));
        assertInstanceOf(PartyBroadcast.Disbanded.class, broadcasts.getLast());
    }

    @Test
    void leavingWithoutPartyReportsError() {
        PartyCache.handleLeave(new PartyAction.Leave(leader));
        assertEquals(List.of(leader), errorTargets);
    }

    @Test
    void onlyLeaderCanDisband() {
        FullParty party = formParty();
        PartyCache.handleDisband(new PartyAction.Disband(member));
        assertEquals(List.of(member), errorTargets);
        assertTrue(PartyCache.isPartyActive(party.getUuid()));

        PartyCache.handleDisband(new PartyAction.Disband(leader));
        assertFalse(PartyCache.isPartyActive(party.getUuid()));
    }

    @Test
    void transferSwapsRoles() {
        FullParty party = formParty();
        PartyCache.handleTransfer(new PartyAction.Transfer(leader, member));
        assertEquals(FullParty.Role.MEMBER, party.getFromUuid(leader).getRole());
        assertEquals(FullParty.Role.LEADER, party.getFromUuid(member).getRole());
        assertEquals(member, party.getLeader().getUuid());
    }

    @Test
    void transferToNonMemberOrByNonLeaderFails() {
        FullParty party = formParty();
        PartyCache.handleTransfer(new PartyAction.Transfer(leader, third));
        PartyCache.handleTransfer(new PartyAction.Transfer(member, leader));
        assertEquals(List.of(leader, member), errorTargets);
        assertEquals(leader, party.getLeader().getUuid());
    }

    @Test
    void promoteAndDemoteChangeRoleOnlyForLeader() {
        FullParty party = formParty();
        PartyCache.handlePromote(new PartyAction.Promote(leader, member));
        assertEquals(FullParty.Role.MODERATOR, party.getFromUuid(member).getRole());

        PartyCache.handleDemote(new PartyAction.Demote(member, leader));
        assertEquals(FullParty.Role.LEADER, party.getFromUuid(leader).getRole());

        PartyCache.handleDemote(new PartyAction.Demote(leader, member));
        assertEquals(FullParty.Role.MEMBER, party.getFromUuid(member).getRole());
    }

    @Test
    void nonLeaderCannotPromote() {
        FullParty party = formParty();
        party.getFromUuid(member).setRole(FullParty.Role.MODERATOR);
        party.getMembers().add(new FullParty.Member(third, FullParty.Role.MEMBER, true));
        PartyCache.registerParty(party);
        PartyCache.handlePromote(new PartyAction.Promote(member, third));
        assertEquals(FullParty.Role.MEMBER, party.getFromUuid(third).getRole());
        assertEquals(List.of(member), errorTargets);
    }

    @Test
    void kickRulesAreEnforced() {
        FullParty party = formParty();
        party.getMembers().add(new FullParty.Member(third, FullParty.Role.MEMBER, true));
        PartyCache.registerParty(party);

        PartyCache.handleKick(new PartyAction.Kick(member, third));
        assertTrue(PartyCache.isInParty(third));

        PartyCache.handleKick(new PartyAction.Kick(leader, leader));
        assertTrue(PartyCache.isInParty(leader));

        PartyCache.handleKick(new PartyAction.Kick(leader, third));
        assertFalse(PartyCache.isInParty(third));
        assertEquals(2, party.getMembers().size());
        assertEquals(2, errorTargets.size());
        assertInstanceOf(PartyBroadcast.MemberKicked.class, broadcasts.getLast());
    }

    @Test
    void changeSizeValidatesBoundsAndCurrentMembers() {
        FullParty party = formParty();
        PartyCache.handleChangeSize(new PartyAction.ChangeSize(leader, FullParty.MAX_SIZE + 1));
        PartyCache.handleChangeSize(new PartyAction.ChangeSize(leader, FullParty.MIN_SIZE - 1));
        assertEquals(FullParty.DEFAULT_SIZE, party.getMaxSize());
        assertEquals(2, errorTargets.size());

        PartyCache.handleChangeSize(new PartyAction.ChangeSize(member, 10));
        assertEquals(3, errorTargets.size());

        PartyCache.handleChangeSize(new PartyAction.ChangeSize(leader, 10));
        assertEquals(10, party.getMaxSize());
    }

    @Test
    void togglesFlipFlagsForLeaderOnly() {
        FullParty party = formParty();
        PartyCache.handleToggleMute(new PartyAction.ToggleMute(leader));
        PartyCache.handleToggleAllInvite(new PartyAction.ToggleAllInvite(leader));
        assertTrue(party.isMuted());
        assertTrue(party.isAllowAllInvite());

        PartyCache.handleToggleMute(new PartyAction.ToggleMute(member));
        assertTrue(party.isMuted());
        assertEquals(List.of(member), errorTargets);

        PartyCache.handleToggleMute(new PartyAction.ToggleMute(leader));
        assertFalse(party.isMuted());
    }

    @Test
    void mutedPartyBlocksPlainMembersChat() {
        FullParty party = formParty();
        party.setMuted(true);
        int before = broadcasts.size();
        PartyCache.handleChat(new PartyAction.Chat(member, "hi"));
        assertEquals(before, broadcasts.size());
        assertEquals(List.of(member), errorTargets);

        PartyCache.handleChat(new PartyAction.Chat(leader, "hi"));
        assertInstanceOf(PartyBroadcast.Chat.class, broadcasts.getLast());
    }

    @Test
    void hijackMakesHijackerLeaderAndDemotesOldLeader() {
        FullParty party = formParty();
        PartyCache.handleHijack(new PartyAction.Hijack(third, leader));
        assertEquals(third, party.getLeader().getUuid());
        assertEquals(FullParty.Role.MEMBER, party.getFromUuid(leader).getRole());
        assertSame(party, PartyCache.getPartyFromPlayer(third));
    }

    @Test
    void hijackingOwnPartyOrNonPartyFails() {
        formParty();
        PartyCache.handleHijack(new PartyAction.Hijack(leader, member));
        PartyCache.handleHijack(new PartyAction.Hijack(third, UUID.randomUUID()));
        assertEquals(List.of(leader, third), errorTargets);
    }

    @Test
    void disconnectAndRejoinManageTimer() {
        formParty();
        PartyCache.handlePlayerDisconnect(new PartyAction.PlayerDisconnect(member));
        assertTrue(PartyCache.hasDisconnectTimer(member));
        assertInstanceOf(PartyBroadcast.MemberDisconnected.class, broadcasts.getLast());

        PartyCache.handlePlayerRejoin(new PartyAction.PlayerRejoin(member));
        assertFalse(PartyCache.hasDisconnectTimer(member));
        assertInstanceOf(PartyBroadcast.MemberRejoined.class, broadcasts.getLast());
    }

    @Test
    void kickOfflineRemovesOnlyDisconnectedMembers() {
        FullParty party = formParty();
        party.getMembers().add(new FullParty.Member(third, FullParty.Role.MEMBER, true));
        PartyCache.registerParty(party);

        PartyCache.handleKickOffline(new PartyAction.KickOffline(leader));
        assertEquals(List.of(leader), errorTargets);

        PartyCache.handlePlayerDisconnect(new PartyAction.PlayerDisconnect(third));
        PartyCache.handleKickOffline(new PartyAction.KickOffline(leader));
        assertFalse(PartyCache.isInParty(third));
        assertTrue(PartyCache.isInParty(member));
        assertFalse(PartyCache.hasDisconnectTimer(third));
    }
}
