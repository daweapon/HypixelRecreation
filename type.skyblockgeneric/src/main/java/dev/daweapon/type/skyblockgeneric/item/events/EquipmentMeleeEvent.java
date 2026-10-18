package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassiveMath;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassives;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.FireDamageTaken;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.FlamingFist;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

public class EquipmentMeleeEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        SkyBlockPlayer player = event.getPlayer();
        if (player.getSkyblockDataHandler() == null) return;
        Set<String> worn = EquipmentPassives.worn(player);
        boolean flamingFist = worn.contains(EquipmentPassiveMath.FLAMING_FIST);
        boolean scourge = worn.contains(EquipmentPassiveMath.SCOURGE_CLOAK);
        if (!flamingFist && !scourge) return;
        if (!EquipmentPassives.meleeReady(player)) return;
        if (scourge) {
            double boosted = damage.doubleValue() + FireDamageTaken.consumeNextHitBonus(player);
            event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
        }
        if (flamingFist) FlamingFist.fire(player, ((PlayerDamageMobValueUpdateEvent) event).getMob());
    }
}
