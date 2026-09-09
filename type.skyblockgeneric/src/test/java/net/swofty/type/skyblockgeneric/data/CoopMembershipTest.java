package net.swofty.type.skyblockgeneric.data;

import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoopMembershipTest {

    @Test
    void pendingInviteesAreNotCoopMembers() {
        UUID owner = UUID.randomUUID();
        UUID invitee = UUID.randomUUID();
        CoopDatabase.Coop coop = CoopDatabase.getClean(owner);
        coop.addInvite(invitee);

        assertTrue(CoopDatabase.isMember(coop, owner));
        assertFalse(CoopDatabase.isMember(coop, invitee));
    }

    @Test
    void aNewCoopStartsWithTheOriginatorAsItsOnlyMember() {
        UUID owner = UUID.randomUUID();
        CoopDatabase.Coop coop = CoopDatabase.getClean(owner);

        assertEquals(List.of(owner), coop.members());
        assertTrue(coop.memberInvites().isEmpty());
    }

    @Test
    void onlyAPendingInviteCanBeAccepted() {
        UUID owner = UUID.randomUUID();
        UUID invitee = UUID.randomUUID();
        CoopDatabase.Coop coop = CoopDatabase.getClean(owner);

        assertFalse(coop.acceptInvite(invitee));
        coop.addInvite(invitee);
        assertTrue(coop.acceptInvite(invitee));
        assertTrue(CoopDatabase.isMember(coop, invitee));
        assertFalse(coop.acceptInvite(invitee));
    }
}
