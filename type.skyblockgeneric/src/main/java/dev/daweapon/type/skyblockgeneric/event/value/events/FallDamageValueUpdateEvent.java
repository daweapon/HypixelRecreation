package dev.daweapon.type.skyblockgeneric.event.value.events;

import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Value-update event for adjusting fall damage before it is applied to a player
public class FallDamageValueUpdateEvent extends ValueUpdateEvent {
    public FallDamageValueUpdateEvent(SkyBlockPlayer player, Object value) {
        super(player, value);
    }
}
