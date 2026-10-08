package net.swofty.type.generic.entity.npc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NPCConversationsTest {
    private final AtomicLong now = new AtomicLong(1_000);
    private final UUID player = UUID.randomUUID();
    private final Object elle = new Object();
    private final Object dean = new Object();
    private NPCConversations conversations;

    @BeforeEach
    void setUp() {
        conversations = new NPCConversations(now::get);
    }

    @Test
    void anotherNpcIsRefusedWhileTalking() {
        conversations.begin(player, elle, 10_000);
        assertTrue(conversations.isInConversation(player));
        assertTrue(conversations.blocksClick(player, dean));
        assertFalse(conversations.blocksClick(player, elle));
    }

    @Test
    void endingTheConversationReleasesTheLock() {
        long token = conversations.begin(player, elle, 10_000);
        conversations.end(player, token);
        assertFalse(conversations.isInConversation(player));
        assertFalse(conversations.blocksClick(player, dean));
    }

    @Test
    void aStaleTokenDoesNotReleaseANewerConversation() {
        long first = conversations.begin(player, elle, 10_000);
        long second = conversations.begin(player, dean, 10_000);
        conversations.end(player, first);
        assertTrue(conversations.isInConversation(player));
        conversations.end(player, second);
        assertFalse(conversations.isInConversation(player));
    }

    @Test
    void releaseClearsEverything() {
        conversations.begin(player, elle, 10_000);
        conversations.release(player);
        assertFalse(conversations.isInConversation(player));
    }

    @Test
    void aConversationThatNeverEndsExpires() {
        conversations.begin(player, elle, 4_000);
        now.addAndGet(4_000 + NPCConversations.GRACE_MILLIS);
        assertTrue(conversations.isInConversation(player));
        now.incrementAndGet();
        assertFalse(conversations.isInConversation(player));
    }

    @Test
    void anOwnerlessConversationBelongsToTheClickedNpc() {
        conversations.startClick(player, elle);
        conversations.begin(player, null, 10_000);
        conversations.finishClick(player);
        assertFalse(conversations.blocksClick(player, elle));
        assertTrue(conversations.blocksClick(player, dean));
    }

    @Test
    void aFollowOnConversationKeepsTheSpeaker() {
        long first = conversations.begin(player, elle, 10_000);
        conversations.begin(player, null, 10_000);
        conversations.end(player, first);
        assertFalse(conversations.blocksClick(player, elle));
        assertTrue(conversations.blocksClick(player, dean));
    }

    @Test
    void playersAreIndependent() {
        conversations.begin(player, elle, 10_000);
        assertFalse(conversations.isInConversation(UUID.randomUUID()));
    }
}
