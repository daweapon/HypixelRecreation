package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryStats;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;

public class AccessoryDamageReductionEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (event.getPlayer().getSkyblockDataHandler() == null) return;
        double multiplier = AccessoryStats.damageTakenMultiplier(event.getPlayer(),
                ((PlayerDamagedByMobValueUpdateEvent) event).getMob());
        if (multiplier == 1) return;
        double reduced = damage.doubleValue() * multiplier;
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }
}
