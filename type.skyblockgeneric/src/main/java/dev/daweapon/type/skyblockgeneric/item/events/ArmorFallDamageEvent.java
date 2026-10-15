package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.FallDamageValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilities;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorWorn;

public class ArmorFallDamageEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return FallDamageValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number number)) return;
        if (!ArmorWorn.helmetIs(event.getPlayer(), "CHICKEN_HEAD")) return;
        event.setValue((float) ArmorAbilities.fallDamage(number.doubleValue()));
    }
}
