package net.swofty.type.generic.entity.npc;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * The one conversation a player may be in at a time, shared by every NPC dialogue system. Each
 * conversation holds a token, so a finished conversation only releases the lock it took, and a
 * deadline, so a conversation whose end never arrives cannot lock the player out for good.
 */
public final class NPCConversations {
    public static final String ALREADY_IN_CONVERSATION = "<c>You're already in a conversation!";
    public static final long GRACE_MILLIS = 5_000;

    private static final NPCConversations SERVER = new NPCConversations(System::currentTimeMillis);

    private final Map<UUID, Conversation> active = new ConcurrentHashMap<>();
    private final Map<UUID, Object> clicking = new ConcurrentHashMap<>();
    private final AtomicLong tokens = new AtomicLong();
    private final LongSupplier clock;

    public NPCConversations(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static NPCConversations server() {
        return SERVER;
    }

    /**
     * Starts a conversation. With no owner it belongs to the NPC the player is clicking, or else
     * continues the owner of the conversation it follows on from.
     */
    public long begin(UUID player, @Nullable Object owner, long expectedMillis) {
        Conversation previous = current(player);
        Object resolved = owner;
        if (resolved == null) resolved = clicking.get(player);
        if (resolved == null && previous != null) resolved = previous.owner();
        long token = tokens.incrementAndGet();
        active.put(player, new Conversation(resolved, token, clock.getAsLong() + Math.max(0, expectedMillis) + GRACE_MILLIS));
        return token;
    }

    public void end(UUID player, long token) {
        active.computeIfPresent(player, (_, conversation) -> conversation.token() == token ? null : conversation);
    }

    public void release(UUID player) {
        active.remove(player);
        clicking.remove(player);
    }

    public boolean isInConversation(UUID player) {
        return current(player) != null;
    }

    /** Whether a click on {@code npc} must be refused because the player is talking to someone else. */
    public boolean blocksClick(UUID player, Object npc) {
        Conversation conversation = current(player);
        return conversation != null && !Objects.equals(conversation.owner(), npc);
    }

    public void startClick(UUID player, Object npc) {
        clicking.put(player, npc);
    }

    public void finishClick(UUID player) {
        clicking.remove(player);
    }

    private @Nullable Conversation current(UUID player) {
        Conversation conversation = active.get(player);
        if (conversation == null) return null;
        if (clock.getAsLong() > conversation.deadline()) {
            active.remove(player, conversation);
            return null;
        }
        return conversation;
    }

    private record Conversation(@Nullable Object owner, long token, long deadline) {
    }
}
