package dev.daweapon.commons.friend.events.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import dev.daweapon.commons.friend.FriendResponseEvent;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;

@Getter
// Friend-system response event: notifies both parties that a pending friend request expired unanswered.
public final class FriendRequestExpiredResponseEvent extends FriendResponseEvent {
    private static final Serializer<FriendRequestExpiredResponseEvent> SERIALIZER =
            new JacksonSerializer<>(FriendRequestExpiredResponseEvent.class);

    private final UUID sender;
    private final UUID target;
    private final String senderName;
    private final String targetName;

    @JsonCreator
    public FriendRequestExpiredResponseEvent(@JsonProperty("sender") UUID sender, @JsonProperty("target") UUID target, @JsonProperty("senderName") String senderName, @JsonProperty("targetName") String targetName) {
        super();
        this.sender = sender;
        this.target = target;
        this.senderName = senderName;
        this.targetName = targetName;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(sender, target);
    }

    @Override
    public Serializer<FriendRequestExpiredResponseEvent> getSerializer() {
        return SERIALIZER;
    }
}
