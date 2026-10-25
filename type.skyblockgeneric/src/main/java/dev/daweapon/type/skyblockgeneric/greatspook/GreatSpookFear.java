package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;

public final class GreatSpookFear {
    public static final String SWORD_ID = "GREAT_SPOOK_SWORD";
    public static final String STAFF_ID = "GREAT_SPOOK_STAFF";
    public static final Set<String> ARMOR = Set.of("GREAT_SPOOK_HELMET", "GREAT_SPOOK_CHESTPLATE",
            "GREAT_SPOOK_LEGGINGS", "GREAT_SPOOK_BOOTS");
    public static final double SWORD_PER_FEAR = 1;

    private GreatSpookFear() {
    }

    public static double swordBonus(double fear) {
        return Math.max(0, fear) * SWORD_PER_FEAR;
    }

    public static boolean frightens(Integer mobLevel, double fear) {
        return mobLevel != null && fear > 0 && mobLevel <= fear;
    }

    public static boolean wearsFullSet(SkyBlockPlayer player) {
        return wearsFullSet(WornArmor.cachedIds(player));
    }

    public static boolean wearsFullSet(List<String> worn) {
        return WornArmor.wearsAll(worn, ARMOR);
    }

    public static double fear(SkyBlockPlayer player) {
        return player.getStatistics().allStatistics().getOverall(ItemStatistic.FEAR);
    }

    public static ItemStatistics finalise(SkyBlockPlayer player, ItemStatistics total) {
        if (!SWORD_ID.equals(WornArmor.id(player.getItemInMainHand()))) return total;
        double bonus = swordBonus(total.getOverall(ItemStatistic.FEAR));
        if (bonus == 0) return total;
        return ItemStatistics.add(total, ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, bonus)
                .withBase(ItemStatistic.STRENGTH, bonus)
                .build());
    }
}
