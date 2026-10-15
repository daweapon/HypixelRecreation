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
// Friend-system event: requests a page of a player's pending incoming friend requests.
public final class FriendRequestsListEvent extends FriendEvent {
    private static final Serializer<FriendRequestsListEvent> SERIALIZER =
            new JacksonSerializer<>(FriendRequestsListEvent.class);

    private final UUID player;
    private final int page;

    @JsonCreator
    public FriendRequestsListEvent(@JsonProperty("player") UUID player, @JsonProperty("page") int page) {
        super();
        this.player = player;
        this.page = page;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(player);
    }

    @Override
    public Serializer<FriendRequestsListEvent> getSerializer() {
        return SERIALIZER;
    }
}
