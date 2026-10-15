package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
