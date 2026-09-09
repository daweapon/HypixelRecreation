package net.swofty.type.skyblockgeneric.event.value.events;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Value-update event for adjusting a player's health regeneration amount before it is applied
public class RegenerationValueUpdateEvent extends ValueUpdateEvent {
    public RegenerationValueUpdateEvent(SkyBlockPlayer player, Object value) {
        super(player, value);
    }
}
