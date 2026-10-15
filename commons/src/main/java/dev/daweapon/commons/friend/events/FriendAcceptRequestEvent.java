package dev.daweapon.commons.friend.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import dev.daweapon.commons.friend.FriendEvent;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;

@Getter
// Friend-system event: a player accepted an incoming friend request.
public final class FriendAcceptRequestEvent extends FriendEvent {
    private static final Serializer<FriendAcceptRequestEvent> SERIALIZER =
            new JacksonSerializer<>(FriendAcceptRequestEvent.class);

    private final UUID accepter;
    private final UUID requester;

    @JsonCreator
    public FriendAcceptRequestEvent(@JsonProperty("accepter") UUID accepter, @JsonProperty("requester") UUID requester) {
        super();
        this.accepter = accepter;
        this.requester = requester;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(accepter, requester);
    }

    @Override
    public Serializer<FriendAcceptRequestEvent> getSerializer() {
        return SERIALIZER;
    }
}
