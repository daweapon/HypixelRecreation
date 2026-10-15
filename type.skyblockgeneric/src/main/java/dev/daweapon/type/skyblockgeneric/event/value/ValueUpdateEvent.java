package dev.daweapon.type.skyblockgeneric.event.value;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Base class for events that let listeners mutate a numeric/object value before it takes effect
public abstract class ValueUpdateEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    @Setter
    private Object value;

    public ValueUpdateEvent(SkyBlockPlayer player, Object value) {
        this.player = player;
        this.value = value;
    }
}
