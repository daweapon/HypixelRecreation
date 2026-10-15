package dev.daweapon.type.skyblockgeneric.event.value.events;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Value-update event for adjusting a player's max health before it is applied
public class MaxHealthValueUpdateEvent extends ValueUpdateEvent {
    public MaxHealthValueUpdateEvent(SkyBlockPlayer player, Object value) {
        super(player, value);
    }
}
