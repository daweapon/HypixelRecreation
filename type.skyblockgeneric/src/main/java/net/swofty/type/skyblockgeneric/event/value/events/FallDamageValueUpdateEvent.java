package net.swofty.type.skyblockgeneric.event.value.events;

import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Value-update event for adjusting fall damage before it is applied to a player
public class FallDamageValueUpdateEvent extends ValueUpdateEvent {
    public FallDamageValueUpdateEvent(SkyBlockPlayer player, Object value) {
        super(player, value);
    }
}
