package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.MaxHealthValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class ElegantTuxedoSet extends SkyBlockValueEvent implements ArmorSet {
    @Override
    public String getName() {
        return "Dashing";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "Max health set to <c>250♥<7>.",
                "Deal <c>+150% <7>damage!",
                "<8>Very stylish."
        ));
    }

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return MaxHealthValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent tempEvent) {
        MaxHealthValueUpdateEvent event = (MaxHealthValueUpdateEvent) tempEvent;

        if (!isWearingSet(event.getPlayer())) return;

        event.setValue(250F);
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.DAMAGE, 150D)
                .build();
    }
}
