package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import net.swofty.type.skyblockgeneric.item.events.AccessorySeaCreatureDamageEvent;
import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;

import java.util.List;

public class BackwaterArmorSet extends SkyBlockValueEvent implements TieredArmorSet {
    @Override
    public String getName() {
        return "Swamp Soldier";
    }

    @Override
    public int minimumPieces() {
        return 1;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        return List.of(
                "<7>Increases damage dealt to <3>Sea",
                "<3>Creatures <7>by <a>" + (long) ArmorAbilityMath.tier(ArmorAbilityMath.SWAMP_SOLDIER_BONUS, wornPieces) + "%<7>.");
    }

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (!AccessorySeaCreatureDamageEvent.isSeaCreature(((PlayerDamageMobValueUpdateEvent) event).getMob())) return;
        double percent = ArmorAbilityMath.tier(ArmorAbilityMath.SWAMP_SOLDIER_BONUS, getWornPieceCount(event.getPlayer()));
        if (percent <= 0) return;
        double boosted = ArmorAbilityMath.boostedByPercent(damage.doubleValue(), percent);
        event.setValue(damage instanceof Float ? (Object) (float) boosted : (Object) boosted);
    }
}
