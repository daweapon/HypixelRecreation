package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

public class EnchantmentTidalEvent extends SkyBlockValueEvent {
    public static final double DEFENSE_PERCENT_PER_LEVEL = 5;

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (!AccessorySeaCreatureDamageEvent.isSeaCreature(((PlayerDamagedByMobValueUpdateEvent) event).getMob())) return;
        SkyBlockEnchantment tidal = new SkyBlockItem(event.getPlayer().getLeggings()).getAttributeHandler()
                .getEnchantment(EnchantmentType.TIDAL);
        if (tidal == null) return;
        double defense = event.getPlayer().getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
        double reduced = reduce(damage.doubleValue(), defense, tidal.level());
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }

    public static double reduce(double damage, double defense, int level) {
        double boosted = defense * (1 + level * DEFENSE_PERCENT_PER_LEVEL / 100D);
        return damage * (100D + Math.max(0, defense)) / (100D + Math.max(0, boosted));
    }
}
