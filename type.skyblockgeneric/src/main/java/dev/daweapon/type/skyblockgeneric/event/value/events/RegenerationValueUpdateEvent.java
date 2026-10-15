package dev.daweapon.type.skyblockgeneric.event.value.events;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Value-update event for adjusting a player's health regeneration amount before it is applied
public class RegenerationValueUpdateEvent extends ValueUpdateEvent {
    public RegenerationValueUpdateEvent(SkyBlockPlayer player, Object value) {
        super(player, value);
    }
}
