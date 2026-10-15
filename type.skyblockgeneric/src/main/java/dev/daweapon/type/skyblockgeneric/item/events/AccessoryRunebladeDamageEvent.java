package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryHandlerEffects;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;

public class AccessoryRunebladeDamageEvent extends SkyBlockValueEvent {
    public static final String HANDLER = "AXE_WOODLAND_DAMAGE";

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        SkyBlockMob mob = ((PlayerDamageMobValueUpdateEvent) event).getMob();
        if (mob == null || !mob.getMobTypes().contains(MobType.WOODLAND)) return;
        if (!new SkyBlockItem(event.getPlayer().getItemInMainHand()).hasComponent(AxeComponent.class)) return;
        double percent = AccessoryHandlerEffects.percent(event.getPlayer(), HANDLER);
        if (percent <= 0) return;
        double boosted = damage.doubleValue() * (1 + percent / 100D);
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
