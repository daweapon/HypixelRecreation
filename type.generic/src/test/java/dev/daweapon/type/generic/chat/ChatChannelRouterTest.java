package dev.daweapon.type.generic.chat;

import dev.daweapon.type.generic.chat.ChatChannelRouter.Route;
import dev.daweapon.type.generic.data.datapoints.DatapointChatType.Chats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatChannelRouterTest {

    @Test
    void staffChannelIsOnlyForStaff() {
        assertEquals(Route.STAFF_SEND, ChatChannelRouter.decide(true, Chats.STAFF, false));
        assertEquals(Route.STAFF_DENIED, ChatChannelRouter.decide(false, Chats.STAFF, false));
    }

    @Test
    void partyChannelNeedsAParty() {
        assertEquals(Route.PARTY_SEND, ChatChannelRouter.decide(false, Chats.PARTY, true));
        assertEquals(Route.PARTY_DENIED, ChatChannelRouter.decide(false, Chats.PARTY, false));
        assertEquals(Route.PARTY_DENIED, ChatChannelRouter.decide(true, Chats.PARTY, false));
    }

    @Test
    void allChannelBroadcastsRegardlessOfRankOrParty() {
        assertEquals(Route.BROADCAST, ChatChannelRouter.decide(false, Chats.ALL, false));
        assertEquals(Route.BROADCAST, ChatChannelRouter.decide(true, Chats.ALL, true));
    }

    @Test
    void nonStaffMessagesLoseNonAsciiCharacters() {
        assertEquals("hello  world", ChatChannelRouter.sanitize("hello é☃ world", false));
    }

    @Test
    void staffMessagesKeepTheirCharacters() {
        assertEquals("hello é☃ world", ChatChannelRouter.sanitize("hello é☃ world", true));
    }
}
