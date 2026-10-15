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
// Friend-system event: requests removal of every friend from a player's list.
public final class FriendRemoveAllRequestEvent extends FriendEvent {
    private static final Serializer<FriendRemoveAllRequestEvent> SERIALIZER =
            new JacksonSerializer<>(FriendRemoveAllRequestEvent.class);

    private final UUID player;

    @JsonCreator
    public FriendRemoveAllRequestEvent(@JsonProperty("player") UUID player) {
        super();
        this.player = player;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(player);
    }

    @Override
    public Serializer<FriendRemoveAllRequestEvent> getSerializer() {
        return SERIALIZER;
    }
}
