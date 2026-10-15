package dev.daweapon.type.skyblockgeneric.event.value.events;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Value-update event for adjusting damage a player deals to a mob before it is applied
public class PlayerDamageMobValueUpdateEvent extends ValueUpdateEvent {
    private final SkyBlockMob mob;

    public PlayerDamageMobValueUpdateEvent(SkyBlockPlayer player, Object value, SkyBlockMob mob) {
        super(player, value);
        this.mob = mob;
    }
}
