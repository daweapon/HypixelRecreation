package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;

// Applies the temporary incoming-damage reduction granted by the Wither Shield/Wither Impact
// abilities (Necron's Blade Scrolls) to mob damage taken by a player.
public class WitherShieldDamageReductionEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        double reduction = event.getPlayer().getStatistics().getIncomingDamageReductionPercent();
        if (reduction <= 0) return;

        if (!(event.getValue() instanceof Number damage)) return;
        double reduced = damage.doubleValue() * (1 - Math.min(reduction, 1.0));
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }
}
