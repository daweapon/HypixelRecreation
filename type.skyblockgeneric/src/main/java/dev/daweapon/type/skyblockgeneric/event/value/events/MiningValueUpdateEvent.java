package dev.daweapon.type.skyblockgeneric.event.value.events;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Value-update event for adjusting mining-related output (e.g. drops/XP) for a specific item before it is applied
public class MiningValueUpdateEvent extends ValueUpdateEvent {
    private final SkyBlockItem item;

    public MiningValueUpdateEvent(SkyBlockPlayer player, Object value, SkyBlockItem item) {
        super(player, value);

        this.item = item;
    }
}
