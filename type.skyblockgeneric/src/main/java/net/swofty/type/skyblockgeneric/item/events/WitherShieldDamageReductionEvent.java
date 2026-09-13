package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;

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

        double value = (double) event.getValue();
        event.setValue(value * (1 - Math.min(reduction, 1.0)));
    }
}
