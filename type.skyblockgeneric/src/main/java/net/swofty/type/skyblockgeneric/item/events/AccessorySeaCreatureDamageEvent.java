package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.skyblockgeneric.accessory.AccessoryHandlerEffects;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.fishing.lava.LavaSeaCreatureMob;

public class AccessorySeaCreatureDamageEvent extends SkyBlockValueEvent {
    public static final String HANDLER = "SEA_CREATURE_DAMAGE_REDUCTION";

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (!isSeaCreature(((PlayerDamagedByMobValueUpdateEvent) event).getMob())) return;
        double percent = AccessoryHandlerEffects.percent(event.getPlayer(), HANDLER);
        if (percent <= 0) return;
        double reduced = damage.doubleValue() * Math.max(0, 1 - percent / 100D);
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }

    public static boolean isSeaCreature(SkyBlockMob mob) {
        return mob instanceof SeaCreatureMob || mob instanceof LavaSeaCreatureMob;
    }
}
