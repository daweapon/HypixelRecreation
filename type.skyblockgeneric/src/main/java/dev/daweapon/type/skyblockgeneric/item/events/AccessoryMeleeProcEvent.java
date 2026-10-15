package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.accessory.handler.RingOfLove;
import dev.daweapon.type.skyblockgeneric.accessory.handler.TarantulaAccessories;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;

public class AccessoryMeleeProcEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (event.getPlayer().getSkyblockDataHandler() == null) return;
        double multiplier = TarantulaAccessories.meleeMultiplier(event.getPlayer(), ((PlayerDamageMobValueUpdateEvent) event).getMob())
                * RingOfLove.damageMultiplier(event.getPlayer());
        if (multiplier == 1) return;
        double boosted = damage.doubleValue() * multiplier;
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
