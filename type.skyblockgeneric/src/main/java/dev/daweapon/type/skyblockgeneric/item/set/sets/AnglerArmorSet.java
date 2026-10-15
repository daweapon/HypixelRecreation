package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import dev.daweapon.type.skyblockgeneric.item.events.AccessorySeaCreatureDamageEvent;
import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class AnglerArmorSet extends SkyBlockValueEvent implements TieredArmorSet {
    @Override
    public String getName() {
        return "Depth Champion";
    }

    @Override
    public int minimumPieces() {
        return 1;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        return List.of(
                "<7>Reduces the damage taken from <3>Sea",
                "<3>Creatures <7>by <a>" + format(ArmorAbilityMath.tier(ArmorAbilityMath.DEPTH_CHAMPION_REDUCTION, wornPieces)) + "%<7>.",
                "",
                "<8>Tiered Bonus: Deepness Within",
                "<7>Gain <c>" + format(ArmorAbilityMath.tier(ArmorAbilityMath.DEEPNESS_WITHIN_HEALTH_PER_LEVEL, wornPieces))
                        + "❤ <7>per Fishing level.");
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int wornPieces) {
        double perLevel = ArmorAbilityMath.tier(ArmorAbilityMath.DEEPNESS_WITHIN_HEALTH_PER_LEVEL, wornPieces);
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, perLevel * player.getSkills().getCurrentLevel(SkillCategories.FISHING))
                .build();
    }

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        if (!AccessorySeaCreatureDamageEvent.isSeaCreature(((PlayerDamagedByMobValueUpdateEvent) event).getMob())) return;
        double percent = ArmorAbilityMath.tier(ArmorAbilityMath.DEPTH_CHAMPION_REDUCTION, getWornPieceCount(event.getPlayer()));
        if (percent <= 0) return;
        double reduced = ArmorAbilityMath.reducedByPercent(damage.doubleValue(), percent);
        event.setValue(damage instanceof Float ? (Object) (float) reduced : (Object) reduced);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
