package dev.daweapon.type.skyblockgeneric.item.armor;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public final class ArmorPassives {
    public static final double LANTERN_DEFENSE_PER_LEVEL = 2;
    public static final double LANTERN_HEALTH_PER_LEVEL = 4;
    public static final double LANTERN_FORTUNE_PER_LEVEL = 1;

    private ArmorPassives() {
    }

    public static ItemStatistics lanternHelmet(int farmingLevel, boolean holdingAxe) {
        int level = Math.max(0, farmingLevel);
        ItemStatistics.Builder builder = ItemStatistics.builder()
                .withBase(ItemStatistic.DEFENSE, level * LANTERN_DEFENSE_PER_LEVEL)
                .withBase(ItemStatistic.HEALTH, level * LANTERN_HEALTH_PER_LEVEL);
        if (holdingAxe) builder.withBase(ItemStatistic.FARMING_FORTUNE, level * LANTERN_FORTUNE_PER_LEVEL);
        return builder.build();
    }

    public static ItemStatistics extraItemStatistics(@Nullable SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        if (wearer == null || item == null || item.isAir()) return ItemStatistics.empty();
        if (!"ENCHANTED_JACK_O_LANTERN".equals(item.getAttributeHandler().getTypeAsString())) return ItemStatistics.empty();
        SkyBlockItem held = new SkyBlockItem(wearer.getInventory().getItemStack(wearer.getHeldSlot()));
        return lanternHelmet(wearer.getSkills().getCurrentLevel(SkillCategories.FARMING),
                !held.isAir() && held.hasComponent(AxeComponent.class));
    }
}
