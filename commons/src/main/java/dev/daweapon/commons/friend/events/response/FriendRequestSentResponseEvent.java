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
// Friend-system response event: confirms to the sender that their friend request was sent.
public final class FriendRequestSentResponseEvent extends FriendResponseEvent {
    private static final Serializer<FriendRequestSentResponseEvent> SERIALIZER =
            new JacksonSerializer<>(FriendRequestSentResponseEvent.class);

    private final UUID sender;
    private final UUID target;
    private final String targetName;

    @JsonCreator
    public FriendRequestSentResponseEvent(@JsonProperty("sender") UUID sender, @JsonProperty("target") UUID target, @JsonProperty("targetName") String targetName) {
        super();
        this.sender = sender;
        this.target = target;
        this.targetName = targetName;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(sender);
    }

    @Override
    public Serializer<FriendRequestSentResponseEvent> getSerializer() {
        return SERIALIZER;
    }
}
