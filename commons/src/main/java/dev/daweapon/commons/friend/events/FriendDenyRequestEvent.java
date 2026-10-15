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
// Friend-system event: a player denied an incoming friend request.
public final class FriendDenyRequestEvent extends FriendEvent {
    private static final Serializer<FriendDenyRequestEvent> SERIALIZER =
            new JacksonSerializer<>(FriendDenyRequestEvent.class);

    private final UUID denier;
    private final UUID requester;

    @JsonCreator
    public FriendDenyRequestEvent(@JsonProperty("denier") UUID denier, @JsonProperty("requester") UUID requester) {
        super();
        this.denier = denier;
        this.requester = requester;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(denier, requester);
    }

    @Override
    public Serializer<FriendDenyRequestEvent> getSerializer() {
        return SERIALIZER;
    }
}
