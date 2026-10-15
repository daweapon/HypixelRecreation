package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryHandlerEffects;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.FallDamageValueUpdateEvent;

/** Feather accessories: {@code SAFE_FALL_BLOCKS} carries blocks in its {@code percent} field, not a percentage. */
public class AccessoryFallDamageEvent extends SkyBlockValueEvent {
    public static final String SAFE_FALL = "SAFE_FALL_BLOCKS";
    public static final String FALL_REDUCTION = "FALL_DAMAGE_REDUCTION";
    private static final double DAMAGE_PER_BLOCK = 2;

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return FallDamageValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        double blocks = AccessoryHandlerEffects.percent(event.getPlayer(), SAFE_FALL);
        double reduction = AccessoryHandlerEffects.percent(event.getPlayer(), FALL_REDUCTION);
        if (blocks <= 0 && reduction <= 0) return;
        event.setValue((float) reduce(damage.doubleValue(), blocks, reduction));
    }

    public static double reduce(double damage, double safeBlocks, double reductionPercent) {
        double afterHeight = Math.max(0, damage - safeBlocks * DAMAGE_PER_BLOCK);
        return afterHeight * Math.max(0, 1 - reductionPercent / 100D);
    }
}
