package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryStats;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;

public class AccessoryDamageBonusEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (event.getPlayer().getSkyblockDataHandler() == null) return;
        double multiplier = AccessoryStats.damageDealtMultiplier(event.getPlayer(),
                ((PlayerDamageMobValueUpdateEvent) event).getMob());
        if (multiplier == 1) return;
        double boosted = damage.doubleValue() * multiplier;
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
