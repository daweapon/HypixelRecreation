package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilities;
import net.swofty.type.skyblockgeneric.item.armor.ArmorWorn;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ArmorDamageDealtEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number number)) return;
        SkyBlockPlayer player = event.getPlayer();
        if (ArmorWorn.countPrefix(player, "REAPER") < ArmorAbilities.TROLLING_PIECES) return;
        SkyBlockMob mob = ((PlayerDamageMobValueUpdateEvent) event).getMob();
        double adjusted = ArmorAbilities.reaperDealt(number.doubleValue(), mob.getMobTypes().contains(MobType.UNDEAD));
        event.setValue(number instanceof Float ? (Object) (float) adjusted : (Object) adjusted);
    }
}
