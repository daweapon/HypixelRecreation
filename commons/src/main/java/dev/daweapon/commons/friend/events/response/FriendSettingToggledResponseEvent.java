package dev.daweapon.commons.friend.events.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import dev.daweapon.commons.friend.FriendResponseEvent;
import dev.daweapon.commons.friend.FriendSettingType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;

@Getter
// Friend-system response event: confirms one of a player's friend-system settings was toggled.
public final class FriendSettingToggledResponseEvent extends FriendResponseEvent {
    private static final Serializer<FriendSettingToggledResponseEvent> SERIALIZER =
            new JacksonSerializer<>(FriendSettingToggledResponseEvent.class);

    private final UUID player;
    private final FriendSettingType settingType;
    private final boolean newValue;

    @JsonCreator
    public FriendSettingToggledResponseEvent(@JsonProperty("player") UUID player, @JsonProperty("settingType") FriendSettingType settingType, @JsonProperty("newValue") boolean newValue) {
        super();
        this.player = player;
        this.settingType = settingType;
        this.newValue = newValue;
    }

    @Override
    public List<UUID> getParticipants() {
        return List.of(player);
    }

    @Override
    public Serializer<FriendSettingToggledResponseEvent> getSerializer() {
        return SERIALIZER;
    }
}
