package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityState;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ArmorAbilityDamageDealtEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        SkyBlockPlayer player = event.getPlayer();
        if (player.getSkyblockDataHandler() == null) return;
        ArmorAbilityState.touchCombat(player);
        var mob = ((PlayerDamageMobValueUpdateEvent) event).getMob();
        if (!mob.getMobTypes().contains(MobType.ENDER) || !ArmorAbilityEffects.darknessActiveNow(player)) return;
        double boosted = ArmorAbilityMath.boostedByPercent(damage.doubleValue(), ArmorAbilityMath.DARKNESS_ENDERMAN_DAMAGE_BONUS);
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
