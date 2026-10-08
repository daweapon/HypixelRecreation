package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoBelt;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// A Dojo belt in the belt slot reduces mob damage taken by its belt's share (0.5% White to 6% Black).
public class DojoBeltDamageReductionEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        DojoBelt belt = equippedBelt(event.getPlayer());
        if (belt == null) return;
        if (!(event.getValue() instanceof Number damage)) return;
        double reduced = damage.doubleValue() * (1 - belt.getDamageReduction());
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }

    private static DojoBelt equippedBelt(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return null;
        UnderstandableSkyBlockItem equipped = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue().getBelt();
        if (equipped == null) return null;
        SkyBlockItem item = new SkyBlockItem(equipped);
        if (item.isNA() || item.isAir()) return null;
        return DojoBelt.of(item.getAttributeHandler().getPotentialType());
    }
}
