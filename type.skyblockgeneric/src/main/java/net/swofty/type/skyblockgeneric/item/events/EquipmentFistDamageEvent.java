package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.equipment.EquipmentBonuses;

public class EquipmentFistDamageEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (event.getPlayer().getSkyblockDataHandler() == null) return;
        if (!EquipmentBonuses.fistsBoosted(event.getPlayer())) return;
        double boosted = damage.doubleValue() * EquipmentBonuses.KNUCKLE_SANDWICH_FIST_MULTIPLIER;
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
